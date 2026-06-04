package com.example.caltracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_entries")
data class FoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val imageUri: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val dateKey: String // format: "2026-05-28"
)

@Entity(tableName = "daily_goals")
data class DailyGoal(
    @PrimaryKey
    val id: Int = 1,
    val calorieGoal: Int = 2000,
    val proteinGoal: Float = 150f,
    val carbsGoal: Float = 250f,
    val fatGoal: Float = 65f
)

@Entity(tableName = "streaks")
data class StreakData(
    @PrimaryKey
    val id: Int = 1,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastLoggedDate: String = ""
)

data class NutritionResult(
    val foodName: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val confidence: String = "high"
)

data class DailySummary(
    val dateKey: String,
    val totalCalories: Int,
    val totalProtein: Float,
    val totalCarbs: Float,
    val totalFat: Float,
    val entries: List<FoodEntry>
)

data class MultiFoodResult(
    val foods: List<NutritionResult>,
    val isMultiple: Boolean = false
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "",
    val age: Int = 25,
    val weightKg: Float = 70f,
    val heightCm: Float = 170f,
    val gender: String = "male", // "male" or "female"
    val activityLevel: String = "moderate", // "sedentary", "light", "moderate", "active", "very_active"
    val goal: String = "maintain", // "lose", "maintain", "gain"
    val isProfileComplete: Boolean = false
)