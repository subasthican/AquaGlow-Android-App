package com.example.aquaglow

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.*

/**
 * AquaGlowWidgetProvider creates and updates the home screen widget
 * Shows habit completion progress, step count, and quick actions
 */
class AquaGlowWidgetProvider : AppWidgetProvider() {
    
    companion object {
        private const val ACTION_MOOD_CLICK = "com.example.aquaglow.MOOD_CLICK"
        private const val ACTION_STEPS_CLICK = "com.example.aquaglow.STEPS_CLICK"
        private const val ACTION_HYDRATION_CLICK = "com.example.aquaglow.HYDRATION_CLICK"
        private const val ACTION_WIDGET_CLICK = "com.example.aquaglow.WIDGET_CLICK"
        const val ACTION_WIDGET_UPDATE = "com.example.aquaglow.WIDGET_UPDATE"
        
        /**
         * Manually refresh all widgets
         */
        fun refreshAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, AquaGlowWidgetProvider::class.java)
            )
            
            val provider = AquaGlowWidgetProvider()
            for (widgetId in widgetIds) {
                provider.updateAppWidget(context, appWidgetManager, widgetId)
            }
        }
    }
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            ACTION_MOOD_CLICK -> {
                // Open app to mood fragment
                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    putExtra("open_mood_fragment", true)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(mainIntent)
            }
            ACTION_STEPS_CLICK -> {
                // Open app to steps fragment
                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    putExtra("open_steps_fragment", true)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(mainIntent)
            }
            ACTION_HYDRATION_CLICK -> {
                // Open app to hydration fragment
                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    putExtra("open_hydration_fragment", true)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(mainIntent)
            }
            ACTION_WIDGET_CLICK -> {
                // Open main app
                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(mainIntent)
            }
            ACTION_WIDGET_UPDATE -> {
                // Refresh all widgets
                refreshAllWidgets(context)
            }
        }
    }
    
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_aquaglow)
        
        // Update time
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        views.setTextViewText(R.id.widgetTime, currentTime)
        
        // Calculate habit progress
        val habitProgress = calculateHabitProgress(context)
        views.setProgressBar(R.id.widgetProgressBar, 100, habitProgress, false)
        views.setTextViewText(R.id.widgetProgressText, "${habitProgress}%")
        
        // Update step count
        val stepCount = SensorManager.getCurrentStepCount(context)
        views.setTextViewText(R.id.widgetStepsText, stepCount.toString())
        
        // Update water intake
        val waterIntake = getWaterIntake(context)
        views.setTextViewText(R.id.widgetWaterText, "${waterIntake}ml")
        
        // Set up click intents
        setupClickIntents(context, views)
        
        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
    
    private fun calculateHabitProgress(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("aquaglow_prefs", Context.MODE_PRIVATE)
        val gson = com.google.gson.Gson()
        
        // Load habits
        val habitsJson = sharedPreferences.getString("habits_list", "[]")
        val type = object : com.google.gson.reflect.TypeToken<List<HabitsFragment.Habit>>() {}.type
        val habits = gson.fromJson<List<HabitsFragment.Habit>>(habitsJson, type) ?: emptyList()
        
        if (habits.isEmpty()) return 0
        
        val completedHabits = habits.count { it.isCompleted }
        return ((completedHabits.toFloat() / habits.size) * 100).toInt()
    }
    
    private fun getWaterIntake(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("aquaglow_prefs", Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return sharedPreferences.getInt("water_intake_ml_$today", 0)
    }
    
    private fun getMoodStatus(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("aquaglow_prefs", Context.MODE_PRIVATE)
        val gson = com.google.gson.Gson()
        
        // Load today's mood
        val moodJson = sharedPreferences.getString("mood_entries", "[]")
        val type = object : com.google.gson.reflect.TypeToken<List<MoodFragment.MoodEntry>>() {}.type
        val moodEntries = gson.fromJson<List<MoodFragment.MoodEntry>>(moodJson, type) ?: emptyList()
        
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayMood = moodEntries.find { 
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.timestamp) == today 
        }
        
        return todayMood?.moodEmoji ?: "😐"
    }
    
    private fun setupClickIntents(context: Context, views: RemoteViews) {
        // Mood button click
        val moodIntent = Intent(context, AquaGlowWidgetProvider::class.java).apply {
            action = ACTION_MOOD_CLICK
        }
        val moodPendingIntent = PendingIntent.getBroadcast(
            context, 0, moodIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widgetMoodButton, moodPendingIntent)
        
        // Steps button click
        val stepsIntent = Intent(context, AquaGlowWidgetProvider::class.java).apply {
            action = ACTION_STEPS_CLICK
        }
        val stepsPendingIntent = PendingIntent.getBroadcast(
            context, 1, stepsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widgetStepsButton, stepsPendingIntent)
        
        // Hydration button click
        val hydrationIntent = Intent(context, AquaGlowWidgetProvider::class.java).apply {
            action = ACTION_HYDRATION_CLICK
        }
        val hydrationPendingIntent = PendingIntent.getBroadcast(
            context, 2, hydrationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widgetHydrationButton, hydrationPendingIntent)
        
        // Widget header click (opens main app)
        val widgetIntent = Intent(context, AquaGlowWidgetProvider::class.java).apply {
            action = ACTION_WIDGET_CLICK
        }
        val widgetPendingIntent = PendingIntent.getBroadcast(
            context, 3, widgetIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widgetTime, widgetPendingIntent)
    }
    
    override fun onEnabled(context: Context) {
        // Called when the first widget is created
        super.onEnabled(context)
        
        // Start sensor tracking if enabled
        if (SensorManager.isSensorTrackingEnabled(context)) {
            SensorManager.startStepCounter(context)
        }
    }
    
    override fun onDisabled(context: Context) {
        // Called when the last widget is removed
        super.onDisabled(context)
        
        // Stop sensor tracking if no widgets are active
        SensorManager.stopStepCounter(context)
    }
}





