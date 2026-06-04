package com.example.caltracker.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FoodEntry::class, DailyGoal::class, StreakData::class, UserProfile::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
}