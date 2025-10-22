package com.example.aquaglow

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * WeatherManager handles weather integration and environmental factors
 */
object WeatherManager {
    
    private const val PREFS_NAME = "aquaglow_prefs"
    private const val WEATHER_KEY = "weather_data"
    private const val WEATHER_SETTINGS_KEY = "weather_settings"
    
    data class WeatherData(
        val temperature: Float,
        val humidity: Int,
        val uvIndex: Int,
        val condition: WeatherCondition,
        val windSpeed: Float,
        val pressure: Float,
        val visibility: Float,
        val timestamp: Long,
        val location: String
    )
    
    enum class WeatherCondition {
        SUNNY, CLOUDY, RAINY, STORMY, SNOWY, FOGGY, WINDY, CLEAR
    }
    
    data class WeatherSettings(
        val isEnabled: Boolean = true,
        val updateInterval: Int = 30, // minutes
        val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
        val showNotifications: Boolean = true,
        val affectMood: Boolean = true,
        val affectHabits: Boolean = true
    )
    
    enum class TemperatureUnit {
        CELSIUS, FAHRENHEIT
    }
    
    data class WeatherRecommendation(
        val type: RecommendationType,
        val title: String,
        val description: String,
        val priority: Priority,
        val icon: String
    )
    
    enum class RecommendationType {
        MOOD, ACTIVITY, HEALTH, CLOTHING, OUTDOOR
    }
    
    enum class Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    /**
     * Get current weather data
     */
    fun getCurrentWeather(context: Context): WeatherData? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val weatherJson = sharedPreferences.getString(WEATHER_KEY, null)
        return if (weatherJson != null) {
            gson.fromJson(weatherJson, WeatherData::class.java)
        } else {
            // Return mock data for demo purposes
            getMockWeatherData()
        }
    }
    
    /**
     * Save weather data
     */
    fun saveWeatherData(context: Context, weatherData: WeatherData) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        sharedPreferences.edit()
            .putString(WEATHER_KEY, gson.toJson(weatherData))
            .apply()
    }
    
    /**
     * Get weather settings
     */
    fun getWeatherSettings(context: Context): WeatherSettings {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val settingsJson = sharedPreferences.getString(WEATHER_SETTINGS_KEY, null)
        return if (settingsJson != null) {
            gson.fromJson(settingsJson, WeatherSettings::class.java)
        } else {
            WeatherSettings()
        }
    }
    
    /**
     * Save weather settings
     */
    fun saveWeatherSettings(context: Context, settings: WeatherSettings) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        sharedPreferences.edit()
            .putString(WEATHER_SETTINGS_KEY, gson.toJson(settings))
            .apply()
    }
    
    /**
     * Generate weather-based recommendations
     */
    fun getWeatherRecommendations(context: Context): List<WeatherRecommendation> {
        val weather = getCurrentWeather(context) ?: return emptyList()
        val recommendations = mutableListOf<WeatherRecommendation>()
        
        // Temperature-based recommendations
        when {
            weather.temperature < 0 -> {
                recommendations.add(WeatherRecommendation(
                    type = RecommendationType.HEALTH,
                    title = "Stay Warm",
                    description = "Very cold weather detected. Dress warmly and consider indoor activities.",
                    priority = Priority.HIGH,
                    icon = "🧥"
                ))
            }
            weather.temperature < 10 -> {
                recommendations.add(WeatherRecommendation(
                    type = RecommendationType.CLOTHING,
                    title = "Layer Up",
                    description = "Cool weather ahead. Wear layers to stay comfortable.",
                    priority = Priority.MEDIUM,
                    icon = "🧣"
                ))
            }
            weather.temperature > 30 -> {
                recommendations.add(WeatherRecommendation(
                    type = RecommendationType.HEALTH,
                    title = "Stay Hydrated",
                    description = "Hot weather detected. Drink plenty of water and avoid prolonged sun exposure.",
                    priority = Priority.HIGH,
                    icon = "💧"
                ))
            }
        }
        
        // UV Index recommendations
        when {
            weather.uvIndex >= 8 -> {
                recommendations.add(WeatherRecommendation(
                    type = RecommendationType.HEALTH,
                    title = "High UV Alert",
                    description = "Very high UV index. Use sunscreen and seek shade during peak hours.",
                    priority = Priority.CRITICAL,
                    icon = "☀️"
                ))
            }
            weather.uvIndex >= 6 -> {
                recommendations.add(WeatherRecommendation(
                    type = RecommendationType.HEALTH,
                    title = "UV Protection",
                    description = "High UV index. Apply sunscreen and wear protective clothing.",
                    priority = Priority.HIGH,
                    icon = "🧴"
                ))
            }
        }
        
        // Weather condition recommendations
        when (weather.condition) {
            WeatherCondition.RAINY -> {
                recommendations.add(WeatherRecommendation(
                    type = RecommendationType.ACTIVITY,
                    title = "Indoor Activities",
                    description = "Rainy weather is perfect for indoor meditation, reading, or gentle exercises.",
                    priority = Priority.MEDIUM,
                    icon = "🌧️"
                ))
            }
            WeatherCondition.SUNNY -> {
                recommendations.add(WeatherRecommendation(
                    type = RecommendationType.OUTDOOR,
                    title = "Great Day for Walking",
                    description = "Sunny weather is perfect for outdoor activities and getting vitamin D.",
                    priority = Priority.MEDIUM,
                    icon = "🌞"
                ))
            }
            WeatherCondition.STORMY -> {
                recommendations.add(WeatherRecommendation(
                    type = RecommendationType.MOOD,
                    title = "Stay Indoors",
                    description = "Stormy weather can affect mood. Consider calming activities like meditation.",
                    priority = Priority.HIGH,
                    icon = "⛈️"
                ))
            }
            WeatherCondition.CLOUDY -> {
                recommendations.add(WeatherRecommendation(
                    type = RecommendationType.MOOD,
                    title = "Cloudy Day Mood",
                    description = "Cloudy weather might affect your mood. Try bright indoor lighting and uplifting activities.",
                    priority = Priority.LOW,
                    icon = "☁️"
                ))
            }
            else -> {}
        }
        
        // Humidity recommendations
        when {
            weather.humidity < 30 -> {
                recommendations.add(WeatherRecommendation(
                    type = RecommendationType.HEALTH,
                    title = "Low Humidity",
                    description = "Dry air detected. Use a humidifier and drink extra water.",
                    priority = Priority.MEDIUM,
                    icon = "💨"
                ))
            }
            weather.humidity > 80 -> {
                recommendations.add(WeatherRecommendation(
                    type = RecommendationType.HEALTH,
                    title = "High Humidity",
                    description = "High humidity can make breathing difficult. Take it easy and stay cool.",
                    priority = Priority.MEDIUM,
                    icon = "💦"
                ))
            }
        }
        
        return recommendations
    }
    
    /**
     * Get weather impact on mood
     */
    fun getWeatherMoodImpact(context: Context): Float {
        val weather = getCurrentWeather(context) ?: return 0f
        var impact = 0f
        
        // Temperature impact
        when {
            weather.temperature in 20f..25f -> impact += 0.2f // Perfect temperature
            weather.temperature in 15f..30f -> impact += 0.1f // Good temperature
            weather.temperature < 5f || weather.temperature > 35f -> impact -= 0.3f // Extreme temperatures
        }
        
        // Weather condition impact
        when (weather.condition) {
            WeatherCondition.SUNNY, WeatherCondition.CLEAR -> impact += 0.3f
            WeatherCondition.CLOUDY -> impact -= 0.1f
            WeatherCondition.RAINY -> impact -= 0.2f
            WeatherCondition.STORMY -> impact -= 0.4f
            WeatherCondition.FOGGY -> impact -= 0.1f
            else -> {}
        }
        
        // UV Index impact (moderate UV is good for mood)
        when {
            weather.uvIndex in 3..7 -> impact += 0.1f
            weather.uvIndex > 10 -> impact -= 0.1f
        }
        
        return impact.coerceIn(-1f, 1f)
    }
    
    /**
     * Get weather-based habit suggestions
     */
    fun getWeatherHabitSuggestions(context: Context): List<String> {
        val weather = getCurrentWeather(context) ?: return emptyList()
        val suggestions = mutableListOf<String>()
        
        when (weather.condition) {
            WeatherCondition.SUNNY -> {
                suggestions.add("Take a 10-minute walk outside")
                suggestions.add("Do outdoor yoga or stretching")
                suggestions.add("Spend time in nature")
            }
            WeatherCondition.RAINY -> {
                suggestions.add("Practice indoor meditation")
                suggestions.add("Read a book or journal")
                suggestions.add("Do gentle indoor exercises")
            }
            WeatherCondition.STORMY -> {
                suggestions.add("Practice deep breathing exercises")
                suggestions.add("Listen to calming music")
                suggestions.add("Do mindfulness activities")
            }
            WeatherCondition.CLOUDY -> {
                suggestions.add("Use bright indoor lighting")
                suggestions.add("Do energizing exercises")
                suggestions.add("Connect with friends or family")
            }
            else -> {}
        }
        
        // Temperature-based suggestions
        when {
            weather.temperature < 10 -> {
                suggestions.add("Drink warm tea or soup")
                suggestions.add("Do indoor cardio exercises")
                suggestions.add("Practice gratitude journaling")
            }
            weather.temperature > 25 -> {
                suggestions.add("Drink extra water")
                suggestions.add("Take cool showers")
                suggestions.add("Avoid outdoor activities during peak heat")
            }
        }
        
        return suggestions
    }
    
    /**
     * Get weather icon
     */
    fun getWeatherIcon(condition: WeatherCondition): String {
        return when (condition) {
            WeatherCondition.SUNNY -> "☀️"
            WeatherCondition.CLOUDY -> "☁️"
            WeatherCondition.RAINY -> "🌧️"
            WeatherCondition.STORMY -> "⛈️"
            WeatherCondition.SNOWY -> "❄️"
            WeatherCondition.FOGGY -> "🌫️"
            WeatherCondition.WINDY -> "💨"
            WeatherCondition.CLEAR -> "🌤️"
        }
    }
    
    /**
     * Get weather description
     */
    fun getWeatherDescription(condition: WeatherCondition): String {
        return when (condition) {
            WeatherCondition.SUNNY -> "Sunny"
            WeatherCondition.CLOUDY -> "Cloudy"
            WeatherCondition.RAINY -> "Rainy"
            WeatherCondition.STORMY -> "Stormy"
            WeatherCondition.SNOWY -> "Snowy"
            WeatherCondition.FOGGY -> "Foggy"
            WeatherCondition.WINDY -> "Windy"
            WeatherCondition.CLEAR -> "Clear"
        }
    }
    
    /**
     * Get mock weather data for demo purposes
     */
    private fun getMockWeatherData(): WeatherData {
        val conditions = WeatherCondition.values()
        val randomCondition = conditions.random()
        
        val random = Random()
        return WeatherData(
            temperature = 15f + random.nextFloat() * 15f,
            humidity = 30 + random.nextInt(61),
            uvIndex = 1 + random.nextInt(11),
            condition = randomCondition,
            windSpeed = random.nextFloat() * 20f,
            pressure = 1000f + random.nextFloat() * 20f,
            visibility = 5f + random.nextFloat() * 10f,
            timestamp = System.currentTimeMillis(),
            location = "Current Location"
        )
    }
}
