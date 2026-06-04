package com.example.caltracker.uii

import com.example.caltracker.data.DailyGoal
import com.example.caltracker.data.FoodEntry
import com.example.caltracker.data.UserProfile
import java.time.LocalTime

// ─── Insight Model ────────────────────────────────────────────────────────────

data class AIInsight(
    val icon: String,
    val title: String,
    val message: String,
    val type: InsightType,
    val priority: Int = 0
)

enum class InsightType {
    SUCCESS, WARNING, INFO, MOTIVATION, ALERT
}

// ─── Insights Engine ──────────────────────────────────────────────────────────

object InsightsEngine {

    fun generateInsights(
        entries: List<FoodEntry>,
        goal: DailyGoal,
        profile: UserProfile?,
        streak: Int,
        totalCalories: Int,
        totalProtein: Float,
        totalCarbs: Float,
        totalFat: Float
    ): List<AIInsight> {
        val insights = mutableListOf<AIInsight>()
        val hour = LocalTime.now().hour
        val name = profile?.name?.ifBlank { "there" } ?: "there"

        val remainingCalories = goal.calorieGoal - totalCalories
        val remainingProtein = goal.proteinGoal - totalProtein
        val remainingCarbs = goal.carbsGoal - totalCarbs
        val remainingFat = goal.fatGoal - totalFat
        val calorieProgress = if (goal.calorieGoal > 0)
            totalCalories.toFloat() / goal.calorieGoal else 0f
        val proteinProgress = if (goal.proteinGoal > 0)
            totalProtein / goal.proteinGoal else 0f

        // ── Streak insights ───────────────────────────────────────────────
        when {
            streak >= 7 -> insights.add(
                AIInsight(
                    "🏆",
                    "Incredible Streak!",
                    "You've logged meals for $streak days straight. You're unstoppable, $name!",
                    InsightType.SUCCESS,
                    priority = 10
                )
            )
            streak >= 3 -> insights.add(
                AIInsight(
                    "🔥",
                    "$streak Day Streak!",
                    "Amazing consistency! Keep it up to beat your record.",
                    InsightType.MOTIVATION,
                    priority = 9
                )
            )
            streak == 0 -> insights.add(
                AIInsight(
                    "💪",
                    "Start Your Streak Today!",
                    "Log your first meal to begin your streak, $name!",
                    InsightType.INFO,
                    priority = 5
                )
            )
        }

        // ── Calorie insights ──────────────────────────────────────────────
        when {
            totalCalories == 0 && hour >= 10 -> insights.add(
                AIInsight(
                    "⚡",
                    "Fuel Up, $name!",
                    "You haven't logged any food yet. Your body needs energy to perform!",
                    InsightType.ALERT,
                    priority = 10
                )
            )
            calorieProgress >= 1f -> insights.add(
                AIInsight(
                    "🎯",
                    "Calorie Goal Reached!",
                    "You've hit your ${goal.calorieGoal} kcal goal for today. Great job!",
                    InsightType.SUCCESS,
                    priority = 8
                )
            )
            calorieProgress >= 0.85f -> insights.add(
                AIInsight(
                    "✅",
                    "Almost There!",
                    "Only $remainingCalories kcal left to hit your daily goal!",
                    InsightType.SUCCESS,
                    priority = 7
                )
            )
            calorieProgress < 0.3f && hour >= 14 -> insights.add(
                AIInsight(
                    "📉",
                    "Calorie Intake Low",
                    "You've only consumed ${(calorieProgress * 100).toInt()}% of your daily calories. Eat more!",
                    InsightType.WARNING,
                    priority = 8
                )
            )
            remainingCalories in 1..500 -> insights.add(
                AIInsight(
                    "🍽️",
                    "$remainingCalories kcal Remaining",
                    "You're doing great! A light snack will complete your day.",
                    InsightType.INFO,
                    priority = 6
                )
            )
        }

        // ── Protein insights ──────────────────────────────────────────────
        when {
            remainingProtein > 50 && hour >= 12 -> insights.add(
                AIInsight(
                    "🥩",
                    "Protein Alert!",
                    "You need ${remainingProtein.toInt()}g more protein today. Add chicken, eggs, or legumes.",
                    InsightType.ALERT,
                    priority = 9
                )
            )
            remainingProtein in 1f..30f -> insights.add(
                AIInsight(
                    "💪",
                    "Almost at Protein Goal!",
                    "Just ${remainingProtein.toInt()}g more protein needed. A Greek yogurt will do it!",
                    InsightType.INFO,
                    priority = 6
                )
            )
            proteinProgress >= 1f -> insights.add(
                AIInsight(
                    "🏋️",
                    "Protein Goal Crushed!",
                    "Excellent! You've hit your protein target of ${goal.proteinGoal.toInt()}g.",
                    InsightType.SUCCESS,
                    priority = 7
                )
            )
        }

        // ── Meal time reminders ───────────────────────────────────────────
        val todayMealTypes = entries.map { getMealType(it.timestamp) }
        when (hour) {
            in 7..9 -> if (!todayMealTypes.contains(MealType.BREAKFAST)) insights.add(
                AIInsight(
                    "🌅",
                    "Breakfast Time!",
                    "Start your day right! Log your breakfast to fuel your morning.",
                    InsightType.INFO,
                    priority = 8
                )
            )
            in 12..13 -> if (!todayMealTypes.contains(MealType.LUNCH)) insights.add(
                AIInsight(
                    "☀️",
                    "Lunch Time!",
                    "It's lunchtime, $name! Don't skip — it keeps your metabolism going.",
                    InsightType.INFO,
                    priority = 8
                )
            )
            in 18..20 -> if (!todayMealTypes.contains(MealType.DINNER)) insights.add(
                AIInsight(
                    "🌙",
                    "Dinner Time!",
                    "Time for dinner! Log your meal to stay on track.",
                    InsightType.INFO,
                    priority = 7
                )
            )
        }

        // ── Hydration reminder ────────────────────────────────────────────
        if (hour in 10..20 && hour % 3 == 0) {
            insights.add(
                AIInsight(
                    "💧",
                    "Stay Hydrated!",
                    "Remember to drink water. Aim for 8 glasses a day for optimal performance.",
                    InsightType.INFO,
                    priority = 4
                )
            )
        }

        // ── Motivational messages ─────────────────────────────────────────
        if (entries.size >= 3) insights.add(
            AIInsight(
                "⭐",
                "Great Logging Today!",
                "You've logged ${entries.size} meals. Consistency is the key to results!",
                InsightType.MOTIVATION,
                priority = 3
            )
        )

        // ── Profile-based insights ────────────────────────────────────────
        profile?.let {
            when (it.goal) {
                "lose" -> if (calorieProgress < 0.9f && hour >= 20) insights.add(
                    AIInsight(
                        "🎯",
                        "Weight Loss On Track!",
                        "You're in a calorie deficit today. Great progress toward your goal!",
                        InsightType.SUCCESS,
                        priority = 6
                    )
                )
                "gain" -> if (calorieProgress < 0.8f && hour >= 18) insights.add(
                    AIInsight(
                        "📈",
                        "Eat More to Gain!",
                        "You need more calories to support muscle growth. Add a protein shake!",
                        InsightType.WARNING,
                        priority = 7
                    )
                )
                "maintain" -> if (calorieProgress in 0.9f..1.1f) insights.add(
                    AIInsight(
                        "⚖️",
                        "Perfect Balance!",
                        "You're right on track with your maintenance calories. Well done!",
                        InsightType.SUCCESS,
                        priority = 5
                    )
                )
            }
        }

        return insights
            .sortedByDescending { it.priority }
            .take(3)
    }
}