package com.example.aquaglow

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log

/**
 * SensorManager utility class for managing step counter and shake detection
 */
object SensorManager {
    
    private const val TAG = "AquaGlowSensorManager"
    private const val SENSOR_ENABLED_KEY = "sensor_tracking_enabled"
    private const val DAILY_STEP_GOAL = "daily_step_goal"
    private const val DEFAULT_STEP_GOAL = 10000
    
    /**
     * Check if sensors are available on the device
     */
    fun areSensorsAvailable(context: Context): Boolean {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        return accelerometer != null
    }
    
    /**
     * Start the step counter service
     */
    fun startStepCounter(context: Context) {
        if (!areSensorsAvailable(context)) {
            Log.w(TAG, "Sensors not available on this device")
            return
        }
        
        val intent = Intent(context, StepCounterService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        Log.d(TAG, "Step counter service started")
    }
    
    /**
     * Stop the step counter service
     */
    fun stopStepCounter(context: Context) {
        val intent = Intent(context, StepCounterService::class.java)
        context.stopService(intent)
        Log.d(TAG, "Step counter service stopped")
    }
    
    /**
     * Check if sensor tracking is enabled
     */
    fun isSensorTrackingEnabled(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("aquaglow_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(SENSOR_ENABLED_KEY, false)
    }
    
    /**
     * Enable or disable sensor tracking
     */
    fun setSensorTrackingEnabled(context: Context, enabled: Boolean) {
        val sharedPreferences = context.getSharedPreferences("aquaglow_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(SENSOR_ENABLED_KEY, enabled).apply()
        
        if (enabled) {
            startStepCounter(context)
        } else {
            stopStepCounter(context)
        }
        
        Log.d(TAG, "Sensor tracking ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Get current step count
     */
    fun getCurrentStepCount(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("aquaglow_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("step_count", 0)
    }
    
    /**
     * Get current shake count
     */
    fun getCurrentShakeCount(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("aquaglow_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("shake_count", 0)
    }
    
    /**
     * Get daily step goal
     */
    fun getDailyStepGoal(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("aquaglow_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt(DAILY_STEP_GOAL, DEFAULT_STEP_GOAL)
    }
    
    /**
     * Set daily step goal
     */
    fun setDailyStepGoal(context: Context, goal: Int) {
        val sharedPreferences = context.getSharedPreferences("aquaglow_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt(DAILY_STEP_GOAL, goal).apply()
        Log.d(TAG, "Daily step goal set to $goal")
    }
    
    /**
     * Get step progress percentage
     */
    fun getStepProgressPercentage(context: Context): Float {
        val currentSteps = getCurrentStepCount(context)
        val goal = getDailyStepGoal(context)
        return if (goal > 0) (currentSteps.toFloat() / goal * 100).coerceAtMost(100f) else 0f
    }
    
    /**
     * Reset daily step count (for midnight reset)
     */
    fun resetDailyStepCount(context: Context) {
        val sharedPreferences = context.getSharedPreferences("aquaglow_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putInt("step_count", 0)
            .putInt("shake_count", 0)
            .putLong("last_activity_update", System.currentTimeMillis())
            .apply()
        Log.d(TAG, "Daily step count reset")
    }
    
    /**
     * FORCE clear all sensor data - used when clearing app data or deleting account
     * This stops the service, clears data, and ensures it stays cleared
     */
    fun forceClearSensorData(context: Context) {
        // Stop the service first
        stopStepCounter(context)
        
        // Clear sensor tracking preference
        val sharedPreferences = context.getSharedPreferences("aquaglow_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putBoolean(SENSOR_ENABLED_KEY, false)
            .putInt("step_count", 0)
            .putInt("shake_count", 0)
            .putLong("last_activity_update", 0)
            .apply()
        
        // Force commit to disk immediately
        sharedPreferences.edit().commit()
        
        Log.d(TAG, "Sensor data force cleared")
    }
    
    /**
     * Get activity summary for statistics
     */
    fun getActivitySummary(context: Context): String {
        val steps = getCurrentStepCount(context)
        val shakes = getCurrentShakeCount(context)
        val goal = getDailyStepGoal(context)
        val progress = getStepProgressPercentage(context)
        
        return "Steps: $steps/$goal (${String.format("%.1f", progress)}%)\nShakes: $shakes"
    }
}
