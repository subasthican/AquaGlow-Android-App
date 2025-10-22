package com.example.aquaglow

import android.animation.ObjectAnimator
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.util.Timer
import kotlin.concurrent.timer

/**
 * Mindfulness Walk Game - Guided walking meditation with step tracking
 */
class MindfulnessGameActivity : BaseGameActivity(), SensorEventListener {
    
    private lateinit var walkTitleText: TextView
    private lateinit var stepsText: TextView
    private lateinit var mindfulnessPromptText: TextView
    private lateinit var timerText: TextView
    private lateinit var progressBar: ProgressBar
    
    private var sensorManager: SensorManager? = null
    private var stepCounterSensor: Sensor? = null
    private var initialSteps = 0
    private var currentSteps = 0
    private var targetSteps = 100
    private var walkTimer: CountDownTimer? = null
    private var promptTimer: Timer? = null
    private var walkDuration = 600 // 10 minutes
    private var timeRemaining = walkDuration
    
    private val mindfulnessPrompts = listOf(
        "Notice your feet touching the ground",
        "Feel the rhythm of your breathing",
        "Observe the world around you",
        "Be present in this moment",
        "Feel each step as it happens",
        "Notice the sensation of movement",
        "Let your thoughts flow naturally",
        "Feel the air on your skin",
        "Listen to the sounds around you",
        "Be grateful for your ability to move",
        "Notice your body's natural balance",
        "Feel the energy in each step",
        "Observe without judgment",
        "Breathe deeply and naturally",
        "Stay present with each movement"
    )
    
    private var currentPromptIndex = 0
    
    override fun setupGame() {
        val gameArea = findViewById<android.widget.FrameLayout>(R.id.gameArea)
        
        // Setup sensor manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        
        // Create main layout
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(32, 32, 32, 32)
        }
        
        // Title
        walkTitleText = TextView(this).apply {
            text = "🚶 Mindful Walking"
            textSize = 24f
            setTextColor(ContextCompat.getColor(context, R.color.primary))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        
        // Timer
        timerText = TextView(this).apply {
            text = formatTime(walkDuration)
            textSize = 32f
            setTextColor(ContextCompat.getColor(context, R.color.text_primary_light))
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 20)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        
        // Steps display
        stepsText = TextView(this).apply {
            text = "0 / $targetSteps steps"
            textSize = 28f
            setTextColor(ContextCompat.getColor(context, R.color.secondary))
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 10)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        
        // Progress bar
        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = targetSteps
            progress = 0
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                40
            ).apply {
                setMargins(20, 10, 20, 30)
            }
            progressTintList = ContextCompat.getColorStateList(context, R.color.secondary)
        }
        
        // Mindfulness prompt
        mindfulnessPromptText = TextView(this).apply {
            text = "Start walking mindfully\nFocus on your breath and steps"
            textSize = 18f
            setTextColor(ContextCompat.getColor(context, R.color.primary_light))
            gravity = Gravity.CENTER
            setPadding(20, 30, 20, 20)
            minHeight = 200
        }
        
        mainLayout.addView(walkTitleText)
        mainLayout.addView(timerText)
        mainLayout.addView(stepsText)
        mainLayout.addView(progressBar)
        mainLayout.addView(mindfulnessPromptText)
        
        gameArea.addView(mainLayout)
        
        // Check if step counter is available
        if (stepCounterSensor == null) {
            android.widget.Toast.makeText(
                this,
                "Step counter not available. Steps will be simulated.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
    
    override fun startGame() {
        isGameRunning = true
        startButton.isEnabled = false
        pauseButton.isEnabled = true
        
        walkTitleText.text = "🚶 Walk Mindfully"
        timeRemaining = walkDuration
        
        // Start step counter
        stepCounterSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        
        // Start walk timer
        startWalkTimer()
        
        // Start showing prompts
        startPromptTimer()
    }
    
    private fun startWalkTimer() {
        walkTimer = object : CountDownTimer((timeRemaining * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = (millisUntilFinished / 1000).toInt()
                timerText.text = formatTime(timeRemaining)
            }
            
            override fun onFinish() {
                endGame()
            }
        }.start()
    }
    
    private fun startPromptTimer() {
        promptTimer = kotlin.concurrent.timer(period = 20000) { // Every 20 seconds
            if (isGameRunning && !isGamePaused) {
                runOnUiThread {
                    showNextPrompt()
                }
            }
        }
    }
    
    private fun showNextPrompt() {
        currentPromptIndex = (currentPromptIndex + 1) % mindfulnessPrompts.size
        val prompt = mindfulnessPrompts[currentPromptIndex]
        
        // Fade out
        mindfulnessPromptText.animate()
            .alpha(0f)
            .setDuration(500)
            .withEndAction {
                mindfulnessPromptText.text = "💭 $prompt"
                // Fade in
                mindfulnessPromptText.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .start()
            }
            .start()
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER && isGameRunning && !isGamePaused) {
            if (initialSteps == 0) {
                initialSteps = event.values[0].toInt()
            }
            
            val totalSteps = event.values[0].toInt()
            currentSteps = totalSteps - initialSteps
            
            updateStepDisplay()
            
            // Check if target reached
            if (currentSteps >= targetSteps && isGameRunning) {
                endGame()
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }
    
    private fun updateStepDisplay() {
        stepsText.text = "$currentSteps / $targetSteps steps"
        progressBar.progress = currentSteps
        
        val percentage = (currentSteps.toFloat() / targetSteps * 100).toInt()
        updateScore(percentage)
        
        // Milestone feedback
        when {
            currentSteps == 25 && currentSteps < targetSteps -> showMilestone("25% Complete! 🌟")
            currentSteps == 50 && currentSteps < targetSteps -> showMilestone("Halfway There! 💪")
            currentSteps == 75 && currentSteps < targetSteps -> showMilestone("Almost Done! 🎯")
        }
    }
    
    private fun showMilestone(message: String) {
        val originalText = mindfulnessPromptText.text
        mindfulnessPromptText.text = message
        mindfulnessPromptText.setTextColor(ContextCompat.getColor(this, R.color.primary))
        
        Handler(Looper.getMainLooper()).postDelayed({
            mindfulnessPromptText.text = originalText
            mindfulnessPromptText.setTextColor(ContextCompat.getColor(this, R.color.primary_light))
        }, 2000)
    }
    
    override fun pauseGame() {
        isGamePaused = true
        pauseButton.text = getString(R.string.resume)
        walkTimer?.cancel()
        promptTimer?.cancel()
        sensorManager?.unregisterListener(this)
        mindfulnessPromptText.text = "Walk Paused\nTake a moment to rest"
    }
    
    override fun resumeGame() {
        isGamePaused = false
        pauseButton.text = getString(R.string.pause)
        
        // Resume step counter
        stepCounterSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        
        startWalkTimer()
        startPromptTimer()
        showNextPrompt()
    }
    
    override fun endGame() {
        isGameRunning = false
        startButton.isEnabled = true
        pauseButton.isEnabled = false
        pauseButton.text = getString(R.string.pause)
        
        walkTimer?.cancel()
        promptTimer?.cancel()
        sensorManager?.unregisterListener(this)
        
        walkTitleText.text = "🎉 Walk Complete!"
        mindfulnessPromptText.text = "Well done! You completed a mindful walk."
        
        // Calculate final score
        val completionBonus = if (currentSteps >= targetSteps) 50 else 0
        val stepScore = (currentSteps.toFloat() / targetSteps * 100).toInt()
        val finalScore = stepScore + completionBonus
        
        updateScore(finalScore)
        
        // Save game result
        GameManager.saveGameScore(this, GameManager.GameScore(
            gameId = "mindfulness_walk",
            score = finalScore,
            maxScore = 150,
            timeSpent = walkDuration - timeRemaining,
            completedAt = System.currentTimeMillis(),
            level = 1,
            stars = if (currentSteps >= targetSteps) 3 else if (currentSteps >= targetSteps * 0.75) 2 else 1
        ))
        
        // Send notification
        sendCompletionNotification(finalScore, currentSteps)
        
        // Show completion dialog
        Handler(Looper.getMainLooper()).postDelayed({
            showCompletionDialog()
        }, 1500)
    }
    
    private fun showCompletionDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🚶 Mindful Walk Complete!")
            .setMessage("Great job!\n\nSteps: $currentSteps / $targetSteps\nTime: ${formatTime(walkDuration - timeRemaining)}\nScore: $gameScore\n\nMindful walking reduces stress and improves awareness!")
            .setPositiveButton("Finish") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun sendCompletionNotification(finalScore: Int, steps: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "game_completion",
                "Game Completion",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for game completion"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = androidx.core.app.NotificationCompat.Builder(this, "game_completion")
            .setSmallIcon(R.drawable.ic_trending_up)
            .setContentTitle("🚶 Mindful Walk Complete!")
            .setContentText("Mindful Walking: $steps steps - $finalScore points earned!")
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle()
                .bigText("Congratulations! You completed a mindful walk\n\nSteps: $steps\nScore: $finalScore\n\nKeep moving mindfully! 🌟"))
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        
        notificationManager.notify(2005, notification)
    }
    
    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        walkTimer?.cancel()
        promptTimer?.cancel()
        sensorManager?.unregisterListener(this)
    }
}




