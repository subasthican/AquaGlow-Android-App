package com.example.aquaglow

import android.content.Context
import android.content.SharedPreferences
import com.github.mikephil.charting.data.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale

/**
 * AnalyticsManager handles advanced analytics and reporting
 */
object AnalyticsManager {
    
    private const val PREFS_NAME = "aquaglow_prefs"
    private const val ANALYTICS_KEY = "analytics_data"
    
    data class AnalyticsData(
        val date: String,
        val habitsCompleted: Int,
        val totalHabits: Int,
        val moodScore: Int,
        val stepCount: Int,
        val hydrationReminders: Int,
        val wellnessScore: Int,
        val sleepHours: Float = 0f,
        val stressLevel: Int = 0,
        val energyLevel: Int = 0
    )
    
    data class TrendData(
        val period: String,
        val averageScore: Float,
        val trend: TrendDirection,
        val improvement: Float
    )
    
    enum class TrendDirection {
        UP, DOWN, STABLE
    }
    
    data class Insight(
        val title: String,
        val description: String,
        val type: InsightType,
        val confidence: Float,
        val recommendation: String
    )
    
    enum class InsightType {
        HABIT, MOOD, FITNESS, SLEEP, STRESS, GENERAL
    }
    
    /**
     * Get analytics data for a date range
     */
    fun getAnalyticsData(context: Context, days: Int = 30): List<AnalyticsData> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val analyticsJson = sharedPreferences.getString(ANALYTICS_KEY, "[]")
        val type = object : TypeToken<List<AnalyticsData>>() {}.type
        val allData = gson.fromJson<List<AnalyticsData>>(analyticsJson, type) ?: emptyList()
        
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        calendar.add(Calendar.DAY_OF_MONTH, -days)
        val startDate = calendar.time
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return allData.filter { data ->
            val dataDate = dateFormat.parse(data.date)
            dataDate != null && dataDate >= startDate && dataDate <= endDate
        }.sortedBy { it.date }
    }
    
    /**
     * Generate mood trend chart data
     */
    fun getMoodTrendData(context: Context, days: Int = 7): LineData {
        val analyticsData = getAnalyticsData(context, days)
        val entries = mutableListOf<Entry>()
        
        analyticsData.forEachIndexed { index, data ->
            entries.add(Entry(index.toFloat(), data.moodScore.toFloat()))
        }
        
        val dataSet = LineDataSet(entries, "Mood Score").apply {
            color = android.graphics.Color.parseColor("#00D4AA")
            setCircleColor(android.graphics.Color.parseColor("#00D4AA"))
            lineWidth = 3f
            circleRadius = 4f
            setDrawFilled(true)
            fillColor = android.graphics.Color.parseColor("#2000D4AA")
        }
        
        return LineData(dataSet)
    }
    
    /**
     * Generate habit completion chart data
     */
    fun getHabitCompletionData(context: Context, days: Int = 7): BarData {
        val analyticsData = getAnalyticsData(context, days)
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        
        analyticsData.forEachIndexed { index, data ->
            val completionRate = if (data.totalHabits > 0) {
                (data.habitsCompleted.toFloat() / data.totalHabits * 100)
            } else 0f
            entries.add(BarEntry(index.toFloat(), completionRate))
            
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(data.date)
            labels.add(dateFormat.format(date ?: Date()))
        }
        
        val dataSet = BarDataSet(entries, "Habit Completion %").apply {
            color = android.graphics.Color.parseColor("#1A1A2E")
            setValueTextColor(android.graphics.Color.parseColor("#00D4AA"))
            valueTextSize = 12f
        }
        
        return BarData(dataSet)
    }
    
    /**
     * Generate wellness score trend
     */
    fun getWellnessScoreTrend(context: Context, days: Int = 30): LineData {
        val analyticsData = getAnalyticsData(context, days)
        val entries = mutableListOf<Entry>()
        
        analyticsData.forEachIndexed { index, data ->
            entries.add(Entry(index.toFloat(), data.wellnessScore.toFloat()))
        }
        
        val dataSet = LineDataSet(entries, "Wellness Score").apply {
            color = android.graphics.Color.parseColor("#FF6B6B")
            setCircleColor(android.graphics.Color.parseColor("#FF6B6B"))
            lineWidth = 3f
            circleRadius = 4f
            setDrawFilled(true)
            fillColor = android.graphics.Color.parseColor("#20FF6B6B")
        }
        
        return LineData(dataSet)
    }
    
    /**
     * Generate step count chart
     */
    fun getStepCountData(context: Context, days: Int = 7): BarData {
        val analyticsData = getAnalyticsData(context, days)
        val entries = mutableListOf<BarEntry>()
        
        analyticsData.forEachIndexed { index, data ->
            entries.add(BarEntry(index.toFloat(), data.stepCount.toFloat()))
        }
        
        val dataSet = BarDataSet(entries, "Daily Steps").apply {
            color = android.graphics.Color.parseColor("#4ECDC4")
            setValueTextColor(android.graphics.Color.parseColor("#4ECDC4"))
            valueTextSize = 12f
        }
        
        return BarData(dataSet)
    }
    
    /**
     * Generate pie chart for habit categories
     */
    fun getHabitCategoryData(context: Context): PieData {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val habitsJson = sharedPreferences.getString("habits_list", "[]")
        val gson = Gson()
        val type = object : TypeToken<List<HabitsFragment.Habit>>() {}.type
        val habits = gson.fromJson<List<HabitsFragment.Habit>>(habitsJson, type) ?: emptyList()
        
        val categoryCount = mutableMapOf<String, Int>()
        habits.forEach { habit ->
            categoryCount[habit.category] = (categoryCount[habit.category] ?: 0) + 1
        }
        
        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()
        
        categoryCount.forEach { (category, count) ->
            entries.add(PieEntry(count.toFloat(), category))
            colors.add(getCategoryColor(category))
        }
        
        val dataSet = PieDataSet(entries, "Habit Categories").apply {
            this.colors = colors
            valueTextSize = 12f
            valueTextColor = android.graphics.Color.WHITE
        }
        
        return PieData(dataSet)
    }
    
    /**
     * Get trend analysis
     */
    fun getTrendAnalysis(context: Context, days: Int = 30): TrendData {
        val analyticsData = getAnalyticsData(context, days)
        if (analyticsData.isEmpty()) {
            return TrendData("No Data", 0f, TrendDirection.STABLE, 0f)
        }
        
        val averageScore = analyticsData.map { it.wellnessScore }.average().toFloat()
        val firstHalf = analyticsData.take(analyticsData.size / 2).map { it.wellnessScore.toFloat() }.average().toFloat()
        val secondHalf = analyticsData.drop(analyticsData.size / 2).map { it.wellnessScore.toFloat() }.average().toFloat()
        
        val improvement = secondHalf - firstHalf
        val trend = when {
            improvement > 5 -> TrendDirection.UP
            improvement < -5 -> TrendDirection.DOWN
            else -> TrendDirection.STABLE
        }
        
        return TrendData("$days days", averageScore, trend, improvement)
    }
    
    /**
     * Generate insights
     */
    fun generateInsights(context: Context): List<Insight> {
        val insights = mutableListOf<Insight>()
        val analyticsData = getAnalyticsData(context, 30)
        
        if (analyticsData.isEmpty()) {
            return insights
        }
        
        val averageMood = analyticsData.map { it.moodScore }.average()
        val averageSteps = analyticsData.map { it.stepCount }.average()
        val averageHabits = analyticsData.map { it.habitsCompleted }.average()
        val totalHabits = analyticsData.map { it.totalHabits }.average()
        
        // Mood insights
        if (averageMood < 5) {
            insights.add(Insight(
                title = "Mood Improvement Opportunity",
                description = "Your average mood score is ${String.format(Locale.getDefault(), "%.1f", averageMood)}/10. Consider activities that boost your mood.",
                type = InsightType.MOOD,
                confidence = 0.8f,
                recommendation = "Try meditation, exercise, or spending time with loved ones."
            ))
        }
        
        // Step count insights
        if (averageSteps < 5000) {
            insights.add(Insight(
                title = "Increase Daily Activity",
                description = "You're averaging ${String.format(Locale.getDefault(), "%.0f", averageSteps)} steps per day. The recommended daily target is 10,000 steps.",
                type = InsightType.FITNESS,
                confidence = 0.9f,
                recommendation = "Take short walks throughout the day or use stairs instead of elevators."
            ))
        }
        
        // Habit completion insights
        val habitCompletionRate = if (totalHabits > 0) (averageHabits / totalHabits * 100) else 0.0
        if (habitCompletionRate < 70) {
            insights.add(Insight(
                title = "Habit Consistency",
                description = "You're completing ${String.format(Locale.getDefault(), "%.0f", habitCompletionRate)}% of your habits. Consistency is key to building lasting habits.",
                type = InsightType.HABIT,
                confidence = 0.85f,
                recommendation = "Start with smaller, more achievable habits and gradually increase complexity."
            ))
        }
        
        // Positive insights
        if (averageMood > 7) {
            insights.add(Insight(
                title = "Great Mood Management!",
                description = "Your mood has been consistently positive. Keep up the great work!",
                type = InsightType.MOOD,
                confidence = 0.9f,
                recommendation = "Continue your current wellness practices."
            ))
        }
        
        return insights
    }
    
    /**
     * Save analytics data
     */
    fun saveAnalyticsData(context: Context, data: AnalyticsData) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val existingData = getAnalyticsData(context, 365).toMutableList()
        
        // Remove existing data for the same date
        existingData.removeAll { it.date == data.date }
        existingData.add(data)
        
        sharedPreferences.edit()
            .putString(ANALYTICS_KEY, gson.toJson(existingData))
            .apply()
    }
    
    private fun getCategoryColor(category: String): Int {
        return when (category.lowercase()) {
            "health" -> android.graphics.Color.parseColor("#FF6B6B")
            "fitness" -> android.graphics.Color.parseColor("#4ECDC4")
            "mindfulness" -> android.graphics.Color.parseColor("#45B7D1")
            "learning" -> android.graphics.Color.parseColor("#96CEB4")
            "social" -> android.graphics.Color.parseColor("#FFEAA7")
            "work" -> android.graphics.Color.parseColor("#DDA0DD")
            else -> android.graphics.Color.parseColor("#95A5A6")
        }
    }
}
