package com.example.caltracker.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caltracker.data.*
import com.example.caltracker.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import android.util.Base64
import android.graphics.BitmapFactory

// ─── UI State ─────────────────────────────────────────────────────────────────

data class HomeUiState(
    val todayEntries: List<FoodEntry> = emptyList(),
    val goal: DailyGoal = DailyGoal(),
    val streak: StreakData = StreakData(),
    val totalCalories: Int = 0,
    val totalProtein: Float = 0f,
    val totalCarbs: Float = 0f,
    val totalFat: Float = 0f
)

data class AnalysisUiState(
    val isLoading: Boolean = false,
    val result: NutritionResult? = null,
    val multiResult: MultiFoodResult? = null,
    val error: String? = null,
    val imageUri: Uri? = null
)

data class ProgressUiState(
    val weeklySummary: List<DailySummaryRaw> = emptyList(),
    val streak: StreakData = StreakData()
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: FoodRepository
) : ViewModel() {

    var apiKey: String = try {
        com.example.caltracker.BuildConfig.GEMINI_API_KEY.also {
            android.util.Log.d("CalTracker", "API Key loaded: ${it.take(8)}...")
        }
    } catch (e: Exception) {
        ""
    }

    private val _homeState = MutableStateFlow(HomeUiState())
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    private val _analysisState = MutableStateFlow(AnalysisUiState())
    val analysisState: StateFlow<AnalysisUiState> = _analysisState.asStateFlow()

    private val _progressState = MutableStateFlow(ProgressUiState())
    val progressState: StateFlow<ProgressUiState> = _progressState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()
    private val _profileState = MutableStateFlow<UserProfile?>(null)
    val profileState: StateFlow<UserProfile?> = _profileState.asStateFlow()

    private val _isFirstLaunch = MutableStateFlow(true)
    val isFirstLaunch: StateFlow<Boolean> = _isFirstLaunch.asStateFlow()

    private val _showSuccess = MutableStateFlow<Pair<String, Int>?>(null)
    val showSuccess: StateFlow<Pair<String, Int>?> = _showSuccess.asStateFlow()

    fun dismissSuccess() {
        _showSuccess.value = null
    }

    init {
        loadHomeData()
        loadProgressData()
        checkProfile()
    }

    // ─── Home ──────────────────────────────────────────────────────────────────

    private fun loadHomeData() {
        viewModelScope.launch {
            combine(
                repository.getTodayEntries(),
                repository.getGoal().map { it ?: DailyGoal() },
                repository.getStreak().map { it ?: StreakData() }
            ) { entries, goal, streak ->
                HomeUiState(
                    todayEntries = entries,
                    goal = goal,
                    streak = streak,
                    totalCalories = entries.sumOf { it.calories },
                    totalProtein = entries.sumOf { it.protein.toDouble() }.toFloat(),
                    totalCarbs = entries.sumOf { it.carbs.toDouble() }.toFloat(),
                    totalFat = entries.sumOf { it.fat.toDouble() }.toFloat()
                )
            }.collect { _homeState.value = it }
        }
    }

    // ─── Progress ──────────────────────────────────────────────────────────────

    private fun loadProgressData() {
        viewModelScope.launch {
            combine(
                repository.getWeeklySummary(),
                repository.getStreak().map { it ?: StreakData() }
            ) { weekly, streak ->
                ProgressUiState(weeklySummary = weekly, streak = streak)
            }.collect { _progressState.value = it }
        }
    }

    private fun checkProfile() {
        viewModelScope.launch {
            repository.getProfile().collect { profile ->
                _profileState.value = profile
                _isFirstLaunch.value = profile == null || !profile.isProfileComplete
            }
        }
    }

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile.copy(isProfileComplete = true))
            _isFirstLaunch.value = false
            _snackbarMessage.emit("Welcome, ${profile.name}! 🎉")
        }
    }

    // ─── AI Analysis ───────────────────────────────────────────────────────────

    fun setImageUri(uri: Uri) {
        _analysisState.value = _analysisState.value.copy(imageUri = uri, result = null, error = null)
    }

    fun analyzeImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            _analysisState.value = _analysisState.value.copy(isLoading = true, error = null)
            try {
                val base64 = uriToBase64(context, uri)
                val result = repository.analyzeFoodImage(base64, "image/jpeg", apiKey)
                result.fold(
                    onSuccess = {
                        _analysisState.value = _analysisState.value.copy(
                            isLoading = false, result = it
                        )
                    },
                    onFailure = {
                        _analysisState.value = _analysisState.value.copy(
                            isLoading = false,
                            error = it.message ?: "Analysis failed"
                        )
                    }
                )
            } catch (e: Exception) {
                _analysisState.value = _analysisState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Something went wrong"
                )
            }
        }
    }

    fun confirmAndSaveEntry(result: NutritionResult) {
        viewModelScope.launch {
            val entry = FoodEntry(
                name = result.foodName,
                calories = result.calories,
                protein = result.protein,
                carbs = result.carbs,
                fat = result.fat,
                imageUri = _analysisState.value.imageUri?.toString() ?: "",
                dateKey = repository.getTodayKey()
            )
            repository.addFoodEntry(entry)
            _analysisState.value = AnalysisUiState()
            // Trigger success animation
            _showSuccess.value = Pair(result.foodName, result.calories)
        }
    }

    fun confirmAndSaveMultipleEntries(results: List<NutritionResult>) {
        viewModelScope.launch {
            results.forEach { result ->
                val entry = FoodEntry(
                    name = result.foodName,
                    calories = result.calories,
                    protein = result.protein,
                    carbs = result.carbs,
                    fat = result.fat,
                    imageUri = _analysisState.value.imageUri?.toString() ?: "",
                    dateKey = repository.getTodayKey()
                )
                repository.addFoodEntry(entry)
            }
            _analysisState.value = AnalysisUiState()
            val totalCals = results.sumOf { it.calories }
            // Trigger success animation
            _showSuccess.value = Pair("${results.size} items", totalCals)
        }
    }

    fun deleteFoodEntry(entry: FoodEntry) {
        viewModelScope.launch {
            repository.deleteFoodEntry(entry)
            _snackbarMessage.emit("Entry removed")
        }
    }

    fun saveGoal(calorieGoal: Int, proteinGoal: Float, carbsGoal: Float, fatGoal: Float) {
        viewModelScope.launch {
            repository.saveGoal(DailyGoal(
                calorieGoal = calorieGoal,
                proteinGoal = proteinGoal,
                carbsGoal = carbsGoal,
                fatGoal = fatGoal
            ))
            _snackbarMessage.emit("Goals updated!")
        }
    }

    fun updateFoodEntry(entry: FoodEntry) {
        viewModelScope.launch {
            repository.updateFoodEntry(entry)
            _snackbarMessage.emit("${entry.name} updated!")
        }
    }

    fun logOut() {
        viewModelScope.launch {
            repository.saveProfile(UserProfile(isProfileComplete = false))
            _isFirstLaunch.value = true
        }
    }

    fun clearAnalysis() {
        _analysisState.value = AnalysisUiState()
    }

    fun analyzeMultipleItems(context: Context, uri: Uri) {
        viewModelScope.launch {
            _analysisState.value = _analysisState.value.copy(
                isLoading = true,
                error = null
            )
            try {
                val base64 = uriToBase64(context, uri)
                val result = repository.analyzeMultipleFoods(base64, "image/jpeg", apiKey)
                result.fold(
                    onSuccess = {
                        _analysisState.value = _analysisState.value.copy(
                            isLoading = false,
                            multiResult = it,
                            result = null
                        )
                    },
                    onFailure = {
                        _analysisState.value = _analysisState.value.copy(
                            isLoading = false,
                            error = it.message ?: "Analysis failed"
                        )
                    }
                )
            } catch (e: Exception) {
                _analysisState.value = _analysisState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Something went wrong"
                )
            }
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private fun uriToBase64(context: Context, uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val resized = resizeBitmap(bitmap, 1024)
        val outputStream = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val ratio = minOf(maxSize.toFloat() / width, maxSize.toFloat() / height)
        return if (ratio < 1) {
            Bitmap.createScaledBitmap(
                bitmap,
                (width * ratio).toInt(),
                (height * ratio).toInt(),
                true
            )
        } else bitmap
    }
}

var apiKey: String = try {
    com.example.caltracker.BuildConfig.GEMINI_API_KEY.also {
        android.util.Log.d("CalTracker", "Key length: ${it.length}, starts: ${it.take(6)}")
    }
} catch (e: Exception) {
    ""
}