package com.example.caltracker.repository

import com.example.caltracker.data.*
import com.example.caltracker.network.ClaudeApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepository @Inject constructor(
    private val foodDao: FoodDao,
    private val claudeApiService: ClaudeApiService
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun getTodayKey(): String = LocalDate.now().format(dateFormatter)

    fun getProfile(): Flow<UserProfile?> = foodDao.getProfile()

    suspend fun getProfileOnce(): UserProfile? = foodDao.getProfileOnce()

    suspend fun saveProfile(profile: UserProfile) {
        foodDao.insertOrUpdateProfile(profile)
        // Auto-calculate and save goals based on profile
        val calories = calculateTDEE(profile)
        val protein = when (profile.goal) {
            "lose" -> profile.weightKg * 2.2f
            "gain" -> profile.weightKg * 2.0f
            else -> profile.weightKg * 1.8f
        }
        val fat = (calories * 0.25f / 9f)
        val carbs = (calories - (protein * 4f) - (fat * 9f)) / 4f

        foodDao.insertOrUpdateGoal(
            DailyGoal(
                calorieGoal = calories,
                proteinGoal = protein,
                carbsGoal = carbs.coerceAtLeast(50f),
                fatGoal = fat
            )
        )
    }

    private fun calculateTDEE(profile: UserProfile): Int {
        // Mifflin-St Jeor equation
        val bmr = if (profile.gender == "male") {
            (10 * profile.weightKg) + (6.25f * profile.heightCm) - (5 * profile.age) + 5
        } else {
            (10 * profile.weightKg) + (6.25f * profile.heightCm) - (5 * profile.age) - 161
        }

        val tdee = bmr * when (profile.activityLevel) {
            "sedentary" -> 1.2f
            "light" -> 1.375f
            "moderate" -> 1.55f
            "active" -> 1.725f
            "very_active" -> 1.9f
            else -> 1.55f
        }

        return when (profile.goal) {
            "lose" -> (tdee - 500).toInt()
            "gain" -> (tdee + 300).toInt()
            else -> tdee.toInt()
        }
    }
    fun getTodayEntries(): Flow<List<FoodEntry>> =
        foodDao.getFoodEntriesByDate(getTodayKey())

    fun getEntriesByDate(dateKey: String): Flow<List<FoodEntry>> =
        foodDao.getFoodEntriesByDate(dateKey)

    fun getWeeklySummary(): Flow<List<DailySummaryRaw>> =
        foodDao.getWeeklySummary()

    fun getGoal(): Flow<DailyGoal?> = foodDao.getGoal()

    fun getStreak(): Flow<StreakData?> = foodDao.getStreak()

    suspend fun addFoodEntry(entry: FoodEntry) {
        foodDao.insertFoodEntry(entry)
        updateStreak()
    }

    suspend fun deleteFoodEntry(entry: FoodEntry) =
        foodDao.deleteFoodEntry(entry)

    suspend fun saveGoal(goal: DailyGoal) =
        foodDao.insertOrUpdateGoal(goal)

    suspend fun updateFoodEntry(entry: FoodEntry) =
        foodDao.insertFoodEntry(entry)

    suspend fun analyzeFoodImage(
        base64Image: String,
        mediaType: String,
        apiKey: String
    ): Result<NutritionResult> =
        claudeApiService.analyzeFoodImage(base64Image, mediaType, apiKey)

    suspend fun analyzeMultipleFoods(
        base64Image: String,
        mediaType: String,
        apiKey: String
    ): Result<MultiFoodResult> =
        claudeApiService.analyzeMultipleFoods(base64Image, mediaType, apiKey)

    private suspend fun updateStreak() {
        val today = getTodayKey()
        val yesterday = LocalDate.now().minusDays(1).format(dateFormatter)
        val streakData = foodDao.getStreak().first() ?: StreakData()
        val newStreak = when (streakData.lastLoggedDate) {
            today -> streakData.currentStreak
            yesterday -> streakData.currentStreak + 1
            else -> 1
        }
        foodDao.insertOrUpdateStreak(
            streakData.copy(
                currentStreak = newStreak,
                longestStreak = maxOf(newStreak, streakData.longestStreak),
                lastLoggedDate = today
            )
        )
    }
}