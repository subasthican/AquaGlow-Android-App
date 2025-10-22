package com.example.aquaglow

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlin.math.sqrt

/**
 * StepCounterService provides step counting and shake detection
 * Uses hardware step counter if available, falls back to accelerometer
 */
class  StepCounterService : Service(), SensorEventListener {
    
    companion object {
        private const val TAG = "StepCounterService"
        private const val NOTIFICATION_ID = 2001
        private const val CHANNEL_ID = "step_counter_channel"
        private const val SHAKE_THRESHOLD = 25.0f  // Increased from 12 - needs harder shake
        private const val SHAKE_TIME_WINDOW = 1000L  // Increased to 1 second cooldown
        private const val MIN_STEP_THRESHOLD = 5  // Minimum steps before counting (filter noise)
    }
    
    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private lateinit var sharedPreferences: android.content.SharedPreferences
    private val handler = Handler(Looper.getMainLooper())
    
    // Step counting
    private var initialStepCount = 0
    private var previousStepCount = 0
    private var currentSteps = 0
    
    // Shake detection
    private var lastShakeTime = 0L
    private var shakeCount = 0
    private var lastUpdate = 0L
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    
    // Periodic update
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateNotification()
            saveData()
            handler.postDelayed(this, 10000) // Update every 10 seconds
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        initializeService()
        startForegroundService()
    }
    
    private fun initializeService() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sharedPreferences = getSharedPreferences("aquaglow_prefs", Context.MODE_PRIVATE)
        
        // Try to use hardware step counter first (more accurate and battery efficient)
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        
        // Fall back to accelerometer if step counter not available
        if (stepCounterSensor == null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            Log.d(TAG, "Using accelerometer for step detection (hardware step counter not available)")
        } else {
            Log.d(TAG, "Using hardware step counter")
        }
        
        // Also register accelerometer for shake detection
        if (accelerometerSensor == null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }
        
        // Load existing step count
        currentSteps = sharedPreferences.getInt("step_count", 0)
        shakeCount = sharedPreferences.getInt("shake_count", 0)
        
        Log.d(TAG, "StepCounterService initialized - Steps: $currentSteps, Shakes: $shakeCount")
    }
    
    private fun startForegroundService() {
        createNotificationChannel()
        
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AquaGlow Activity Tracker")
            .setContentText("Steps: $currentSteps | Shakes: $shakeCount")
            .setSmallIcon(R.drawable.ic_trending_up)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        startForeground(NOTIFICATION_ID, notification)
        Log.d(TAG, "Foreground service started")
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Activity Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Tracks steps and shake gestures for wellness insights"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Register step counter sensor (if available)
        stepCounterSensor?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            Log.d(TAG, "Step counter sensor registered")
        }
        
        // Register accelerometer for shake detection and fallback step counting
        accelerometerSensor?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME // Faster sampling for better detection
            )
            Log.d(TAG, "Accelerometer sensor registered")
        }
        
        // Start periodic updates
        handler.post(updateRunnable)
        
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        handler.removeCallbacks(updateRunnable)
        saveData()
        Log.d(TAG, "StepCounterService destroyed - Final steps: $currentSteps")
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            when (sensorEvent.sensor.type) {
                Sensor.TYPE_STEP_COUNTER -> {
                    // Hardware step counter (cumulative since last reboot)
                    val totalStepsSinceReboot = sensorEvent.values[0].toInt()
                    
                    if (initialStepCount == 0) {
                        // First reading - set initial value
                        initialStepCount = totalStepsSinceReboot - currentSteps
                        previousStepCount = totalStepsSinceReboot
                        Log.d(TAG, "Initial step count set: $initialStepCount")
                    } else {
                        // Calculate steps since service started
                        val newSteps = totalStepsSinceReboot - initialStepCount
                        val stepIncrease = newSteps - currentSteps
                        
                        // Only count if valid increase and above noise threshold
                        if (stepIncrease > 0 && stepIncrease < 100 && newSteps >= MIN_STEP_THRESHOLD) {
                            currentSteps = newSteps
                            Log.d(TAG, "Steps updated: $currentSteps (increase: $stepIncrease)")
                            
                            // Check for milestones (only for significant steps)
                            if (currentSteps >= 10) {
                                checkStepMilestones()
                            }
                        } else if (stepIncrease > 100) {
                            // Reset if unrealistic jump (sensor error)
                            Log.w(TAG, "Unrealistic step increase detected: $stepIncrease, resetting")
                            initialStepCount = totalStepsSinceReboot - currentSteps
                        }
                    }
                }
                
                Sensor.TYPE_ACCELEROMETER -> {
                    val x = sensorEvent.values[0]
                    val y = sensorEvent.values[1]
                    val z = sensorEvent.values[2]
                    
                    // Detect shake
                    detectShake(x, y, z)
                    
                    lastX = x
                    lastY = y
                    lastZ = z
                }
            }
        }
    }
    
    private fun checkStepMilestones() {
        // Notify on significant milestones
        when (currentSteps) {
            1000, 2500, 5000, 7500, 10000 -> {
                showStepMilestoneNotification()
            }
        }
    }
    
    private fun detectShake(x: Float, y: Float, z: Float) {
        val currentTime = System.currentTimeMillis()
        
        // Only check shake if enough time has passed since last update
        if (currentTime - lastUpdate < 100) return
        
        val deltaTime = currentTime - lastUpdate
        lastUpdate = currentTime
        
        // Calculate change in acceleration
        val deltaX = kotlin.math.abs(x - lastX)
        val deltaY = kotlin.math.abs(y - lastY)
        val deltaZ = kotlin.math.abs(z - lastZ)
        
        // Calculate total acceleration change (more accurate)
        val totalDelta = deltaX + deltaY + deltaZ
        
        // Calculate shake force (normalized)
        val shakeForce = totalDelta / deltaTime * 10000
        
        // Require significant movement in multiple axes (not just vibration)
        val hasSignificantMovement = (deltaX > 3.0f || deltaY > 3.0f || deltaZ > 3.0f)
        
        // Check if it's a real shake
        if (shakeForce > SHAKE_THRESHOLD && 
            hasSignificantMovement &&
            currentTime - lastShakeTime > SHAKE_TIME_WINDOW) {
            
            shakeCount++
            lastShakeTime = currentTime
            Log.d(TAG, "Shake detected! Force: $shakeForce, Total shakes: $shakeCount")
            
            // Show mood logging suggestion on shake
            showMoodLoggingSuggestion()
            saveData()
        }
    }
    
    private fun showStepMilestoneNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 1, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🎉 Step Milestone!")
            .setContentText("You've reached $currentSteps steps! Great job staying active!")
            .setSmallIcon(R.drawable.ic_trending_up)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(2002, notification)
    }
    
    private fun showMoodLoggingSuggestion() {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("open_mood_fragment", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 2, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("💭 How are you feeling?")
            .setContentText("You shook your device - want to log your mood?")
            .setSmallIcon(R.drawable.ic_mood)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(2003, notification)
    }
    
    private fun updateNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val goal = com.example.aquaglow.SensorManager.getDailyStepGoal(this)
        val progress = if (goal > 0) (currentSteps * 100 / goal).coerceAtMost(100) else 0
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AquaGlow Activity Tracker")
            .setContentText("Steps: $currentSteps/$goal ($progress%) | Shakes: $shakeCount")
            .setSmallIcon(R.drawable.ic_trending_up)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun saveData() {
        sharedPreferences.edit()
            .putInt("step_count", currentSteps)
            .putInt("shake_count", shakeCount)
            .putLong("last_activity_update", System.currentTimeMillis())
            .apply()
        Log.d(TAG, "Data saved - Steps: $currentSteps, Shakes: $shakeCount")
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used for accelerometer
    }
    
    override fun onBind(intent: Intent): IBinder? {
        return null
    }
    
    /**
     * Get current step count
     */
    fun getStepCount(): Int = currentSteps
    
    /**
     * Get current shake count
     */
    fun getShakeCount(): Int = shakeCount
    
    /**
     * Reset step count (for daily reset)
     */
    fun resetStepCount() {
        currentSteps = 0
        shakeCount = 0
        initialStepCount = 0
        previousStepCount = 0
        saveData()
        updateNotification()
        Log.d(TAG, "Step count reset")
    }
}
