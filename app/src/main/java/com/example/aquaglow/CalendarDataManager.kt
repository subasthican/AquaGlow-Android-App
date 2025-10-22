package com.example.aquaglow

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manages calendar data by aggregating habits, mood, water intake, and achievements by date
 */
object CalendarDataManager {
    
    private const val PREFS_NAME = "aquaglow_prefs"
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    /**
     * Daily log data model
     */
    data class DailyLog(
        val date: String,
        val habitsCompleted: Int = 0,
        val totalHabits: Int = 0,
        val moodEmoji: String? = null,
        val moodNote: String? = null,
        val waterGlasses: Int = 0,
        val waterGoal: Int = 8,
        val stepCount: Int = 0,
        val shakeCount: Int = 0,
        val notes: String? = null,
        // Achievement system removed
    ) {
        val completionPercentage: Int
            get() = if (totalHabits > 0) (habitsCompleted * 100 / totalHabits) else 0
        
        val hasData: Boolean
            get() = habitsCompleted > 0 || moodEmoji != null || waterGlasses > 0 || notes != null
        
        val statusColor: DayStatus
            get() = when {
                completionPercentage >= 100 -> DayStatus.COMPLETE
                completionPercentage >= 50 -> DayStatus.PARTIAL
                completionPercentage > 0 -> DayStatus.PARTIAL
                totalHabits > 0 -> DayStatus.MISSED
                else -> DayStatus.NO_DATA
            }
    }
    
    /**
     * Day status enum for color coding
     */
    enum class DayStatus {
        COMPLETE,   // Green - All habits completed
        PARTIAL,    // Yellow - Some habits completed
        MISSED,     // Red - Habits exist but none completed
        NO_DATA,    // Gray - No habits set for this day
        // ACHIEVEMENT removed
    }
    
    /**
     * Monthly summary data
     */
    data class MonthlySummary(
        val month: String,
        val year: Int,
        val totalDays: Int,
        val daysWithData: Int,
        val habitCompletionRate: Int, // Percentage
        val averageMoodScore: Float, // 1-5 scale
        val totalWaterGlasses: Int,
        val longestStreak: Int,
        val currentStreak: Int,
        // Achievement system removed
        val mostFrequentMood: String?
    )
    
    /**
     * Get daily log for a specific date
     */
    fun getDailyLog(context: Context, date: Date): DailyLog {
        return try {
            val dateString = dateFormat.format(date)
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            // Get habits for this date
            val (habitsCompleted, totalHabits) = getHabitsForDate(prefs, dateString)
            
            // Get mood for this date
            val (moodEmoji, moodNote) = getMoodForDate(prefs, dateString)
            
            // Get water intake for this date
            val waterAmountMl = prefs.getInt("water_intake_ml_$dateString", 0)
            val waterGlasses = waterAmountMl / 250 // Convert ml to glasses (assuming 250ml per glass)
            val waterGoal = prefs.getInt("water_daily_goal", 8)
            
            // Get step and shake count for this date
            val stepCount = if (dateString == dateFormat.format(Date())) {
                try {
                    SensorManager.getCurrentStepCount(context)
                } catch (e: Exception) {
                    0
                }
            } else {
                0 // Historical data not stored yet
            }
            
            val shakeCount = if (dateString == dateFormat.format(Date())) {
                try {
                    SensorManager.getCurrentShakeCount(context)
                } catch (e: Exception) {
                    0
                }
            } else {
                0
            }
            
            // Achievement system removed
            
            DailyLog(
                date = dateString,
                habitsCompleted = habitsCompleted,
                totalHabits = totalHabits,
                moodEmoji = moodEmoji,
                moodNote = moodNote,
                waterGlasses = waterGlasses,
                waterGoal = waterGoal,
                stepCount = stepCount,
                shakeCount = shakeCount,
                // Achievement system removed
            )
        } catch (e: Exception) {
            android.util.Log.e("CalendarDataManager", "Error getting daily log: ${e.message}", e)
            // Return empty log on error
            DailyLog(
                date = dateFormat.format(date),
                habitsCompleted = 0,
                totalHabits = 0,
                moodEmoji = null,
                moodNote = null,
                waterGlasses = 0,
                waterGoal = 8,
                stepCount = 0,
                shakeCount = 0,
                // Achievement system removed
            )
        }
    }
    
    /**
     * Get habits completion for a specific date
     */
    private fun getHabitsForDate(prefs: SharedPreferences, date: String): Pair<Int, Int> {
        // Convert date from yyyy-MM-dd to yyyyMMdd format for habits storage
        val habitsDate = date.replace("-", "")
        val progressKey = "habits_progress_$habitsDate"
        val progressJson = prefs.getString(progressKey, "{}")
        val type = object : TypeToken<Map<String, Boolean>>() {}.type
        val progress = gson.fromJson<Map<String, Boolean>>(progressJson, type) ?: emptyMap()
        
        val habitsJson = prefs.getString("habits_list", "[]")
        val habitsType = object : TypeToken<List<Map<String, Any>>>() {}.type
        val habits = gson.fromJson<List<Map<String, Any>>>(habitsJson, habitsType) ?: emptyList()
        
        val completed = progress.values.count { it }
        val total = habits.size
        
        return Pair(completed, total)
    }
    
    /**
     * Get mood for a specific date
     */
    private fun getMoodForDate(prefs: SharedPreferences, date: String): Pair<String?, String?> {
        try {
            val moodEntriesJson = prefs.getString("mood_entries", "[]")
            val type = object : TypeToken<List<MoodEntry>>() {}.type
            val moodEntries = gson.fromJson<List<MoodEntry>>(moodEntriesJson, type) ?: emptyList()
            
            // Find mood entry for this date
            val entry = moodEntries.firstOrNull { mood ->
                val moodDate = dateFormat.format(mood.timestamp)
                moodDate == date
            }
            
            return if (entry != null) {
                Pair(entry.moodEmoji, entry.note)
            } else {
                Pair(null, null)
            }
        } catch (e: Exception) {
            android.util.Log.e("CalendarDataManager", "Error getting mood for date $date: ${e.message}", e)
            return Pair(null, null)
        }
    }
    
    /**
     * Mood entry data class to match MoodFragment structure
     */
    data class MoodEntry(
        val id: String,
        val moodEmoji: String,
        val moodName: String,
        val moodScore: Int,
        val note: String,
        val timestamp: Date
    )
    
    // Achievement system removed
    
    /**
     * Get monthly summary for a specific month and year
     */
    fun getMonthlySummary(context: Context, month: Int, year: Int): MonthlySummary {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)
        
        val totalDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        var daysWithData = 0
        var totalHabitsCompleted = 0
        var totalHabits = 0
        var totalWaterGlasses = 0
        val moodScores = mutableListOf<Float>()
        val moodEmojis = mutableListOf<String>()
        
        // Iterate through all days in the month
        for (day in 1..totalDays) {
            calendar.set(year, month, day)
            val date = calendar.time
            val log = getDailyLog(context, date)
            
            if (log.hasData) {
                daysWithData++
            }
            
            totalHabitsCompleted += log.habitsCompleted
            totalHabits += log.totalHabits
            totalWaterGlasses += log.waterGlasses
            
            if (log.moodEmoji != null) {
                moodEmojis.add(log.moodEmoji)
                moodScores.add(getMoodScore(log.moodEmoji))
            }
        }
        
        val habitCompletionRate = if (totalHabits > 0) {
            (totalHabitsCompleted * 100 / totalHabits)
        } else 0
        
        val averageMoodScore = if (moodScores.isNotEmpty()) {
            moodScores.average().toFloat()
        } else 0f
        
        val mostFrequentMood = moodEmojis.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
        
        // Calculate streaks
        val (currentStreak, longestStreak) = calculateStreaks(context, month, year)
        
        // Achievement system removed
        val achievementsUnlocked = 0
        
        val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.time)
        
        return MonthlySummary(
            month = monthName,
            year = year,
            totalDays = totalDays,
            daysWithData = daysWithData,
            habitCompletionRate = habitCompletionRate,
            averageMoodScore = averageMoodScore,
            totalWaterGlasses = totalWaterGlasses,
            longestStreak = longestStreak,
            currentStreak = currentStreak,
            // Achievement system removed
            mostFrequentMood = mostFrequentMood
        )
    }
    
    /**
     * Convert mood emoji to numeric score (1-5)
     */
    private fun getMoodScore(emoji: String): Float {
        return when (emoji) {
            "😢" -> 1f // Very sad
            "😐" -> 2f // Neutral
            "😊" -> 3f // Happy
            "😄" -> 4f // Excited
            "🤩" -> 5f // Amazing
            "😠" -> 1.5f // Angry
            else -> 3f // Default to neutral
        }
    }
    
    /**
     * Calculate current and longest streaks for habit completion
     */
    private fun calculateStreaks(context: Context, month: Int, year: Int): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        
        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 0
        
        // Count backwards from today to find streaks
        for (i in 0 until 30) {
            val date = Date(calendar.timeInMillis - (i * 24 * 60 * 60 * 1000))
            val log = getDailyLog(context, date)
            
            if (log.completionPercentage >= 80) { // At least 80% completion
                tempStreak++
                if (i == 0) currentStreak = tempStreak
                if (tempStreak > longestStreak) longestStreak = tempStreak
            } else {
                if (i == 0) currentStreak = 0
                tempStreak = 0
            }
        }
        
        return Pair(currentStreak, longestStreak)
    }
    
    /**
     * Get all logs for a specific month
     */
    fun getMonthLogs(context: Context, month: Int, year: Int): Map<Int, DailyLog> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)
        val totalDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        val logs = mutableMapOf<Int, DailyLog>()
        for (day in 1..totalDays) {
            calendar.set(year, month, day)
            val date = calendar.time
            logs[day] = getDailyLog(context, date)
        }
        
        return logs
    }
}


