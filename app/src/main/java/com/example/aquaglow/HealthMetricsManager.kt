package com.example.aquaglow

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale

/**
 * HealthMetricsManager handles additional health tracking options
 */
object HealthMetricsManager {
    
    private const val PREFS_NAME = "aquaglow_prefs"
    private const val HEALTH_METRICS_KEY = "health_metrics"
    private const val METRICS_SETTINGS_KEY = "metrics_settings"
    
    data class HealthMetric(
        val id: String,
        val name: String,
        val type: MetricType,
        val unit: String,
        val value: Float,
        val target: Float? = null,
        val minValue: Float = 0f,
        val maxValue: Float = 100f,
        val timestamp: Long,
        val notes: String = ""
    )
    
    enum class MetricType {
        WEIGHT, HEIGHT, BMI, BLOOD_PRESSURE, HEART_RATE, BLOOD_SUGAR, 
        SLEEP_HOURS, WATER_INTAKE, CALORIES, PROTEIN, CARBS, FAT,
        VITAMIN_D, IRON, CALCIUM, STRESS_LEVEL, ENERGY_LEVEL, PAIN_LEVEL
    }
    
    data class MetricSettings(
        val isEnabled: Boolean = true,
        val trackingFrequency: TrackingFrequency = TrackingFrequency.DAILY,
        val reminderTime: String = "09:00",
        val targetValues: Map<String, Float> = emptyMap(),
        val showInDashboard: Boolean = true
    )
    
    enum class TrackingFrequency {
        DAILY, WEEKLY, MONTHLY, MANUAL
    }
    
    data class HealthInsight(
        val metricType: MetricType,
        val title: String,
        val description: String,
        val recommendation: String,
        val priority: Priority,
        val trend: TrendDirection
    )
    
    enum class Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    enum class TrendDirection {
        IMPROVING, DECLINING, STABLE, FLUCTUATING
    }
    
    /**
     * Save health metric
     */
    fun saveHealthMetric(context: Context, metric: HealthMetric) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val metricsJson = sharedPreferences.getString(HEALTH_METRICS_KEY, "[]")
        val type = object : TypeToken<List<HealthMetric>>() {}.type
        val metrics = gson.fromJson<List<HealthMetric>>(metricsJson, type)?.toMutableList() ?: mutableListOf()
        
        metrics.add(metric)
        
        // Keep only last 1000 entries per metric type
        val filteredMetrics = metrics.groupBy { it.type }
            .mapValues { (_, metrics) -> metrics.takeLast(1000) }
            .values.flatten()
        
        sharedPreferences.edit()
            .putString(HEALTH_METRICS_KEY, gson.toJson(filteredMetrics))
            .apply()
    }
    
    /**
     * Get health metrics by type
     */
    fun getHealthMetrics(context: Context, type: MetricType, days: Int = 30): List<HealthMetric> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val metricsJson = sharedPreferences.getString(HEALTH_METRICS_KEY, "[]")
        val typeToken = object : TypeToken<List<HealthMetric>>() {}.type
        val allMetrics = gson.fromJson<List<HealthMetric>>(metricsJson, typeToken) ?: emptyList()
        
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return allMetrics.filter { 
            it.type == type && it.timestamp >= cutoffTime 
        }.sortedBy { it.timestamp }
    }
    
    /**
     * Get latest health metric by type
     */
    fun getLatestHealthMetric(context: Context, type: MetricType): HealthMetric? {
        return getHealthMetrics(context, type, 365).maxByOrNull { it.timestamp }
    }
    
    /**
     * Get all health metrics
     */
    fun getAllHealthMetrics(context: Context, days: Int = 30): List<HealthMetric> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val metricsJson = sharedPreferences.getString(HEALTH_METRICS_KEY, "[]")
        val type = object : TypeToken<List<HealthMetric>>() {}.type
        val allMetrics = gson.fromJson<List<HealthMetric>>(metricsJson, type) ?: emptyList()
        
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return allMetrics.filter { it.timestamp >= cutoffTime }.sortedBy { it.timestamp }
    }
    
    /**
     * Get all metrics (alias for getAllHealthMetrics)
     */
    fun getAllMetrics(context: Context): List<HealthMetric> {
        return getAllHealthMetrics(context, 30)
    }
    
    /**
     * Add health metric (alias for saveHealthMetric)
     */
    fun addHealthMetric(context: Context, metric: HealthMetric) {
        saveHealthMetric(context, metric)
    }
    
    /**
     * Delete health metric
     */
    fun deleteHealthMetric(context: Context, metricId: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val metricsJson = sharedPreferences.getString(HEALTH_METRICS_KEY, "[]")
        val type = object : TypeToken<List<HealthMetric>>() {}.type
        val metrics = gson.fromJson<List<HealthMetric>>(metricsJson, type)?.toMutableList() ?: mutableListOf()
        
        metrics.removeAll { it.id == metricId }
        
        sharedPreferences.edit()
            .putString(HEALTH_METRICS_KEY, gson.toJson(metrics))
            .apply()
    }
    
    /**
     * Calculate overall health score
     */
    fun calculateHealthScore(context: Context): Int {
        val allMetrics = getAllHealthMetrics(context, 30)
        if (allMetrics.isEmpty()) return 0
        
        val scores = allMetrics.map { calculateMetricScore(it) }
        return (scores.average()).toInt()
    }
    
    /**
     * Get count of tracked metrics
     */
    fun getTrackedMetricsCount(context: Context): Int {
        val allMetrics = getAllHealthMetrics(context, 30)
        return allMetrics.distinctBy { it.type }.size
    }
    
    /**
     * Get metric settings
     */
    fun getMetricSettings(context: Context): Map<MetricType, MetricSettings> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val settingsJson = sharedPreferences.getString(METRICS_SETTINGS_KEY, "{}")
        val type = object : TypeToken<Map<String, MetricSettings>>() {}.type
        val settingsMap = gson.fromJson<Map<String, MetricSettings>>(settingsJson, type) ?: emptyMap()
        
        return MetricType.values().associateWith { type ->
            settingsMap[type.name] ?: MetricSettings()
        }
    }
    
    /**
     * Save metric settings
     */
    fun saveMetricSettings(context: Context, type: MetricType, settings: MetricSettings) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val settingsJson = sharedPreferences.getString(METRICS_SETTINGS_KEY, "{}")
        val settingsType = object : TypeToken<Map<String, MetricSettings>>() {}.type
        val settingsMap = gson.fromJson<Map<String, MetricSettings>>(settingsJson, settingsType)?.toMutableMap() ?: mutableMapOf()
        
        settingsMap[type.name] = settings
        
        sharedPreferences.edit()
            .putString(METRICS_SETTINGS_KEY, gson.toJson(settingsMap))
            .apply()
    }
    
    /**
     * Generate health insights
     */
    fun generateHealthInsights(context: Context): List<HealthInsight> {
        val insights = mutableListOf<HealthInsight>()
        
        // BMI insights
        val latestWeight = getLatestHealthMetric(context, MetricType.WEIGHT)
        val latestHeight = getLatestHealthMetric(context, MetricType.HEIGHT)
        if (latestWeight != null && latestHeight != null) {
            val bmi = calculateBMI(latestWeight.value, latestHeight.value)
            val bmiInsight = when {
                bmi < 18.5 -> HealthInsight(
                    metricType = MetricType.BMI,
                    title = "Underweight",
                    description = "Your BMI is ${String.format(Locale.getDefault(), "%.1f", bmi)}, which is below the healthy range.",
                    recommendation = "Consider consulting a healthcare provider about healthy weight gain strategies.",
                    priority = Priority.MEDIUM,
                    trend = TrendDirection.STABLE
                )
                bmi > 30 -> HealthInsight(
                    metricType = MetricType.BMI,
                    title = "Obesity Risk",
                    description = "Your BMI is ${String.format("%.1f", bmi)}, which indicates obesity.",
                    recommendation = "Focus on a balanced diet and regular exercise. Consider consulting a healthcare provider.",
                    priority = Priority.HIGH,
                    trend = TrendDirection.STABLE
                )
                bmi in 25f..30f -> HealthInsight(
                    metricType = MetricType.BMI,
                    title = "Overweight",
                    description = "Your BMI is ${String.format("%.1f", bmi)}, which is above the healthy range.",
                    recommendation = "Consider making lifestyle changes to reach a healthier weight.",
                    priority = Priority.MEDIUM,
                    trend = TrendDirection.STABLE
                )
                else -> HealthInsight(
                    metricType = MetricType.BMI,
                    title = "Healthy Weight",
                    description = "Your BMI is ${String.format("%.1f", bmi)}, which is in the healthy range.",
                    recommendation = "Great job! Continue maintaining your healthy lifestyle.",
                    priority = Priority.LOW,
                    trend = TrendDirection.STABLE
                )
            }
            insights.add(bmiInsight)
        }
        
        // Sleep insights
        val sleepMetrics = getHealthMetrics(context, MetricType.SLEEP_HOURS, 7)
        if (sleepMetrics.isNotEmpty()) {
            val averageSleep = sleepMetrics.map { it.value }.average()
            val sleepInsight = when {
                averageSleep < 6 -> HealthInsight(
                    metricType = MetricType.SLEEP_HOURS,
                    title = "Insufficient Sleep",
                    description = "You're averaging ${String.format("%.1f", averageSleep)} hours of sleep per night.",
                    recommendation = "Aim for 7-9 hours of sleep per night for optimal health.",
                    priority = Priority.HIGH,
                    trend = TrendDirection.STABLE
                )
                averageSleep > 9 -> HealthInsight(
                    metricType = MetricType.SLEEP_HOURS,
                    title = "Excessive Sleep",
                    description = "You're averaging ${String.format("%.1f", averageSleep)} hours of sleep per night.",
                    recommendation = "Consider if you're getting quality sleep. Excessive sleep can indicate health issues.",
                    priority = Priority.MEDIUM,
                    trend = TrendDirection.STABLE
                )
                else -> HealthInsight(
                    metricType = MetricType.SLEEP_HOURS,
                    title = "Good Sleep Pattern",
                    description = "You're averaging ${String.format("%.1f", averageSleep)} hours of sleep per night.",
                    recommendation = "Excellent! Continue maintaining your healthy sleep schedule.",
                    priority = Priority.LOW,
                    trend = TrendDirection.STABLE
                )
            }
            insights.add(sleepInsight)
        }
        
        // Water intake insights
        val waterMetrics = getHealthMetrics(context, MetricType.WATER_INTAKE, 7)
        if (waterMetrics.isNotEmpty()) {
            val averageWater = waterMetrics.map { it.value }.average()
            val waterInsight = when {
                averageWater < 1.5 -> HealthInsight(
                    metricType = MetricType.WATER_INTAKE,
                    title = "Low Water Intake",
                    description = "You're averaging ${String.format("%.1f", averageWater)} liters of water per day.",
                    recommendation = "Aim for 2-3 liters of water per day for optimal hydration.",
                    priority = Priority.MEDIUM,
                    trend = TrendDirection.STABLE
                )
                averageWater > 4 -> HealthInsight(
                    metricType = MetricType.WATER_INTAKE,
                    title = "High Water Intake",
                    description = "You're averaging ${String.format("%.1f", averageWater)} liters of water per day.",
                    recommendation = "Be cautious of overhydration. 2-3 liters per day is usually sufficient.",
                    priority = Priority.LOW,
                    trend = TrendDirection.STABLE
                )
                else -> HealthInsight(
                    metricType = MetricType.WATER_INTAKE,
                    title = "Good Hydration",
                    description = "You're averaging ${String.format("%.1f", averageWater)} liters of water per day.",
                    recommendation = "Great job staying hydrated! Continue your current water intake.",
                    priority = Priority.LOW,
                    trend = TrendDirection.STABLE
                )
            }
            insights.add(waterInsight)
        }
        
        // Stress level insights
        val stressMetrics = getHealthMetrics(context, MetricType.STRESS_LEVEL, 7)
        if (stressMetrics.isNotEmpty()) {
            val averageStress = stressMetrics.map { it.value }.average()
            val stressInsight = when {
                averageStress > 7 -> HealthInsight(
                    metricType = MetricType.STRESS_LEVEL,
                    title = "High Stress Levels",
                    description = "Your average stress level is ${String.format("%.1f", averageStress)}/10.",
                    recommendation = "Consider stress management techniques like meditation, exercise, or professional help.",
                    priority = Priority.HIGH,
                    trend = TrendDirection.STABLE
                )
                averageStress < 3 -> HealthInsight(
                    metricType = MetricType.STRESS_LEVEL,
                    title = "Low Stress Levels",
                    description = "Your average stress level is ${String.format("%.1f", averageStress)}/10.",
                    recommendation = "Excellent stress management! Continue your current practices.",
                    priority = Priority.LOW,
                    trend = TrendDirection.STABLE
                )
                else -> HealthInsight(
                    metricType = MetricType.STRESS_LEVEL,
                    title = "Moderate Stress",
                    description = "Your average stress level is ${String.format("%.1f", averageStress)}/10.",
                    recommendation = "Consider incorporating more stress-relief activities into your routine.",
                    priority = Priority.MEDIUM,
                    trend = TrendDirection.STABLE
                )
            }
            insights.add(stressInsight)
        }
        
        return insights
    }
    
    /**
     * Calculate BMI
     */
    private fun calculateBMI(weight: Float, height: Float): Float {
        val heightInMeters = height / 100f
        return weight / (heightInMeters * heightInMeters)
    }
    
    /**
     * Calculate BMI and save as metric
     */
    fun calculateAndSaveBMI(context: Context, weight: Float, height: Float) {
        val bmi = calculateBMI(weight, height)
        val bmiMetric = HealthMetric(
            id = java.util.UUID.randomUUID().toString(),
            name = "BMI",
            type = MetricType.BMI,
            unit = "kg/m²",
            value = bmi,
            timestamp = System.currentTimeMillis(),
            notes = "Calculated from weight and height"
        )
        saveHealthMetric(context, bmiMetric)
    }
    
    /**
     * Convert weight between units
     */
    fun convertWeight(value: Float, fromUnit: String, toUnit: String): Float {
        return when {
            fromUnit == toUnit -> value
            fromUnit == "kg" && toUnit == "lbs" -> value * 2.20462f
            fromUnit == "lbs" && toUnit == "kg" -> value / 2.20462f
            else -> value
        }
    }
    
    /**
     * Get weight trend over time
     */
    fun getWeightTrend(context: Context, days: Int = 30): TrendDirection {
        val weightMetrics = getHealthMetrics(context, MetricType.WEIGHT, days)
        if (weightMetrics.size < 2) return TrendDirection.STABLE
        
        val recent = weightMetrics.takeLast(7).map { it.value }.average()
        val older = weightMetrics.take(7).map { it.value }.average()
        
        val change = recent - older
        val threshold = older * 0.02f // 2% change threshold
        
        return when {
            change > threshold -> TrendDirection.IMPROVING
            change < -threshold -> TrendDirection.DECLINING
            else -> TrendDirection.STABLE
        }
    }
    
    /**
     * Get weight goal progress
     */
    fun getWeightGoalProgress(context: Context): Float {
        val latestWeight = getLatestHealthMetric(context, MetricType.WEIGHT)
        if (latestWeight == null || latestWeight.target == null) return 0f
        
        val current = latestWeight.value
        val target = latestWeight.target!!
        val start = latestWeight.minValue
        
        return when {
            target > start -> (current - start) / (target - start) * 100f
            target < start -> (start - current) / (start - target) * 100f
            else -> 100f
        }.coerceIn(0f, 100f)
    }
    
    /**
     * Get metric trend
     */
    fun getMetricTrend(context: Context, type: MetricType, days: Int = 7): TrendDirection {
        val metrics = getHealthMetrics(context, type, days)
        if (metrics.size < 2) return TrendDirection.STABLE
        
        val firstHalf = metrics.take(metrics.size / 2).map { it.value }.average()
        val secondHalf = metrics.drop(metrics.size / 2).map { it.value }.average()
        
        val difference = secondHalf - firstHalf
        val threshold = (metrics.map { it.value }.maxOrNull() ?: 0f) * 0.05f // 5% threshold
        
        // For metrics where lower is better, invert the improvement direction
        val lowerIsBetter = type in listOf(
            MetricType.STRESS_LEVEL, 
            MetricType.PAIN_LEVEL,
            MetricType.BLOOD_PRESSURE,
            MetricType.BLOOD_SUGAR
        )
        
        return when {
            difference > threshold -> if (lowerIsBetter) TrendDirection.DECLINING else TrendDirection.IMPROVING
            difference < -threshold -> if (lowerIsBetter) TrendDirection.IMPROVING else TrendDirection.DECLINING
            else -> TrendDirection.STABLE
        }
    }
    
    /**
     * Get metric statistics
     */
    fun getMetricStatistics(context: Context, type: MetricType, days: Int = 30): Map<String, Any> {
        val metrics = getHealthMetrics(context, type, days)
        
        if (metrics.isEmpty()) {
            return mapOf(
                "count" to 0,
                "average" to 0f,
                "min" to 0f,
                "max" to 0f,
                "trend" to TrendDirection.STABLE
            )
        }
        
        val values = metrics.map { it.value }
        return mapOf(
            "count" to metrics.size,
            "average" to values.average().toFloat(),
            "min" to (values.minOrNull() ?: 0f),
            "max" to (values.maxOrNull() ?: 0f),
            "trend" to getMetricTrend(context, type, days)
        )
    }
    
    /**
     * Get default metrics for new users
     */
    private fun getDefaultMetrics(): List<HealthMetric> {
        val currentTime = System.currentTimeMillis()
        return listOf(
            HealthMetric(
                id = "weight_1",
                name = "Weight",
                type = MetricType.WEIGHT,
                unit = "kg",
                value = 70f,
                target = 70f,
                minValue = 40f,
                maxValue = 150f,
                timestamp = currentTime,
                notes = "Default weight entry"
            ),
            HealthMetric(
                id = "height_1",
                name = "Height",
                type = MetricType.HEIGHT,
                unit = "cm",
                value = 170f,
                target = 170f,
                minValue = 100f,
                maxValue = 250f,
                timestamp = currentTime,
                notes = "Default height entry"
            ),
            HealthMetric(
                id = "sleep_1",
                name = "Sleep Hours",
                type = MetricType.SLEEP_HOURS,
                unit = "hours",
                value = 8f,
                target = 8f,
                minValue = 0f,
                maxValue = 24f,
                timestamp = currentTime,
                notes = "Default sleep entry"
            )
        )
    }
    
    /**
     * Calculate score for a specific metric
     */
    private fun calculateMetricScore(metric: HealthMetric): Int {
        return when (metric.type) {
            // Weight scoring based on target value
            MetricType.WEIGHT -> {
                metric.target?.let { target ->
                    val diff = kotlin.math.abs(metric.value - target)
                    val tolerance = target * 0.1f // 10% tolerance
                    when {
                        diff <= tolerance -> 100
                        diff <= tolerance * 2 -> 80
                        diff <= tolerance * 3 -> 60
                        else -> 40
                    }
                } ?: 75 // Default if no target set
            }
            
            MetricType.HEIGHT -> 100 // Height is neutral
            
            // BMI scoring
            MetricType.BMI -> {
                when {
                    metric.value < 18.5 -> 60
                    metric.value in 18.5..24.9 -> 100
                    metric.value in 25.0..29.9 -> 80
                    else -> 40
                }
            }
            
            // Blood pressure (systolic)
            MetricType.BLOOD_PRESSURE -> {
                when {
                    metric.value < 90 -> 60
                    metric.value <= 120 -> 100
                    metric.value <= 140 -> 70
                    else -> 40
                }
            }
            
            // Heart rate (bpm)
            MetricType.HEART_RATE -> {
                when {
                    metric.value < 60 -> 70
                    metric.value <= 100 -> 100
                    metric.value <= 120 -> 70
                    else -> 50
                }
            }
            
            // Blood sugar (mg/dL)
            MetricType.BLOOD_SUGAR -> {
                when {
                    metric.value < 70 -> 60
                    metric.value <= 100 -> 100
                    metric.value <= 125 -> 70
                    else -> 40
                }
            }
            
            // Sleep hours
            MetricType.SLEEP_HOURS -> {
                when {
                    metric.value in 7f..9f -> 100
                    metric.value in 6f..10f -> 80
                    metric.value in 5f..11f -> 60
                    else -> 40
                }
            }
            
            // Water intake (liters)
            MetricType.WATER_INTAKE -> {
                when {
                    metric.value >= 2f -> 100
                    metric.value >= 1.5f -> 80
                    metric.value >= 1f -> 60
                    else -> 40
                }
            }
            
            // Stress level (lower is better, 0-10 scale)
            MetricType.STRESS_LEVEL -> {
                when {
                    metric.value <= 3 -> 100
                    metric.value <= 5 -> 80
                    metric.value <= 7 -> 60
                    else -> 40
                }
            }
            
            // Energy level (higher is better, 0-10 scale)
            MetricType.ENERGY_LEVEL -> {
                when {
                    metric.value >= 8 -> 100
                    metric.value >= 6 -> 80
                    metric.value >= 4 -> 60
                    else -> 40
                }
            }
            
            // Pain level (lower is better, 0-10 scale)
            MetricType.PAIN_LEVEL -> {
                when {
                    metric.value <= 2 -> 100
                    metric.value <= 4 -> 80
                    metric.value <= 6 -> 60
                    else -> 40
                }
            }
            
            // Calories (depends on target)
            MetricType.CALORIES -> {
                metric.target?.let { target ->
                    val diff = kotlin.math.abs(metric.value - target)
                    val tolerance = target * 0.1f
                    when {
                        diff <= tolerance -> 100
                        diff <= tolerance * 2 -> 80
                        else -> 60
                    }
                } ?: 75
            }
            
            // Nutrients (depends on target)
            MetricType.PROTEIN, MetricType.CARBS, MetricType.FAT,
            MetricType.VITAMIN_D, MetricType.IRON, MetricType.CALCIUM -> {
                metric.target?.let { target ->
                    val percentage = (metric.value / target) * 100
                    when {
                        percentage >= 90 && percentage <= 110 -> 100
                        percentage >= 80 && percentage <= 120 -> 80
                        percentage >= 60 && percentage <= 140 -> 60
                        else -> 40
                    }
                } ?: 75
            }
        }
    }
    
}
