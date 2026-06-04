package com.example.caltracker.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.caltracker.data.NutritionResult
import com.example.caltracker.data.MultiFoodResult
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ClaudeApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeFoodImage(
        base64Image: String,
        mediaType: String = "image/jpeg",
        apiKey: String
    ): Result<NutritionResult> = withContext(Dispatchers.IO) {
        try {
            val requestBody = JSONObject().apply {
                put("contents", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", org.json.JSONArray().apply {
                            // Image part
                            put(JSONObject().apply {
                                put("inline_data", JSONObject().apply {
                                    put("mime_type", mediaType)
                                    put("data", base64Image)
                                })
                            })
                            // Text part
                            put(JSONObject().apply {
                                put("text", """
                                    Analyze this food image and return ONLY a JSON object.
                                    No markdown, no backticks, no extra text. Just raw JSON:
                                    {
                                      "foodName": "name of the food",
                                      "calories": 000,
                                      "protein": 00.0,
                                      "carbs": 00.0,
                                      "fat": 00.0,
                                      "confidence": "high"
                                    }
                                    Base values on a standard single serving size.
                                    All numeric values must be numbers not strings.
                                """.trimIndent())
                            })
                        })
                    })
                })
            }.toString()

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
                ?: return@withContext Result.failure(Exception("Empty response"))

            val json = JSONObject(responseBody)

            // Check for API errors
            if (json.has("error")) {
                val error = json.getJSONObject("error").getString("message")
                return@withContext Result.failure(Exception("API Error: $error"))
            }

            val text = json
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val result = Gson().fromJson(text, NutritionResult::class.java)
            Result.success(result)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun analyzeMultipleFoods(
        base64Image: String,
        mediaType: String = "image/jpeg",
        apiKey: String
    ): Result<MultiFoodResult> = withContext(Dispatchers.IO) {
        try {
            val requestBody = JSONObject().apply {
                put("contents", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", org.json.JSONArray().apply {
                            put(JSONObject().apply {
                                put("inline_data", JSONObject().apply {
                                    put("mime_type", mediaType)
                                    put("data", base64Image)
                                })
                            })
                            put(JSONObject().apply {
                                put("text", """
                                Analyze this food image carefully.
                                Identify ALL separate food items visible on the plate/image.
                                Return ONLY a JSON object with no markdown, no backticks:
                                {
                                  "isMultiple": true,
                                  "foods": [
                                    {
                                      "foodName": "name of food 1",
                                      "calories": 000,
                                      "protein": 00.0,
                                      "carbs": 00.0,
                                      "fat": 00.0,
                                      "confidence": "high"
                                    },
                                    {
                                      "foodName": "name of food 2",
                                      "calories": 000,
                                      "protein": 00.0,
                                      "carbs": 00.0,
                                      "fat": 00.0,
                                      "confidence": "high"
                                    }
                                  ]
                                }
                                If there is only one food item, still use the same format
                                but with a single item in the foods array.
                                Base values on standard single serving sizes.
                                All numeric values must be numbers not strings.
                            """.trimIndent())
                            })
                        })
                    })
                })
            }.toString()

            val request = okhttp3.Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
                ?: return@withContext Result.failure(Exception("Empty response"))

            val json = JSONObject(responseBody)

            if (json.has("error")) {
                val error = json.getJSONObject("error").getString("message")
                return@withContext Result.failure(Exception("API Error: $error"))
            }

            val text = json
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val result = Gson().fromJson(text, MultiFoodResult::class.java)
            Result.success(result)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

