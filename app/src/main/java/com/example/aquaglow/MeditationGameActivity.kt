package com.example.aquaglow

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

/**
 * Meditation Timer Game - Guided meditation with timer and calming visuals
 */
class MeditationGameActivity : BaseGameActivity() {
    
    private lateinit var meditationCircle: View
    private lateinit var instructionText: TextView
    private lateinit var timerText: TextView
    private lateinit var breathGuideText: TextView
    private lateinit var sessionTypeText: TextView
    
    private var meditationTimer: CountDownTimer? = null
    private var breathCycleTimer: CountDownTimer? = null
    private var currentPhase = BreathPhase.INHALE
    private var cyclesCompleted = 0
    private var sessionDuration = 300 // 5 minutes in seconds
    private var breathInDuration = 4000L
    private var holdDuration = 4000L
    private var breathOutDuration = 6000L
    
    enum class BreathPhase {
        INHALE, HOLD, EXHALE, PAUSE
    }
    
    override fun setupGame() {
        val gameArea = findViewById<android.widget.FrameLayout>(R.id.gameArea)
        
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
        
        // Session type text
        sessionTypeText = TextView(this).apply {
            text = "🧘 Guided Meditation Session"
            textSize = 20f
            setTextColor(ContextCompat.getColor(context, R.color.primary))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 40)
        }
        
        // Timer display
        timerText = TextView(this).apply {
            text = formatTime(sessionDuration)
            textSize = 32f
            setTextColor(ContextCompat.getColor(context, R.color.text_primary_light))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        
        // Meditation circle (pulsing visual)
        meditationCircle = View(this).apply {
            setBackgroundResource(R.drawable.breathing_circle)
            layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                gravity = Gravity.CENTER
                setMargins(0, 40, 0, 40)
            }
            alpha = 0.7f
        }
        
        // Breath guide text
        breathGuideText = TextView(this).apply {
            text = "Breathe In..."
            textSize = 24f
            setTextColor(ContextCompat.getColor(context, R.color.secondary))
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 20)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        
        // Instruction text
        instructionText = TextView(this).apply {
            text = "Find a comfortable position\nClose your eyes if you wish\nFocus on your breathing"
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, R.color.text_secondary_light))
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 0)
        }
        
        // Add views to layout
        mainLayout.addView(sessionTypeText)
        mainLayout.addView(timerText)
        mainLayout.addView(meditationCircle)
        mainLayout.addView(breathGuideText)
        mainLayout.addView(instructionText)
        
        gameArea.addView(mainLayout)
    }
    
    override fun startGame() {
        isGameRunning = true
        startButton.isEnabled = false
        pauseButton.isEnabled = true
        
        sessionTypeText.text = "🧘 Meditation in Progress..."
        instructionText.text = "Follow the breathing guide\nLet your thoughts flow naturally"
        
        // Start meditation timer
        startMeditationTimer()
        
        // Start breathing guide
        startBreathCycle()
    }
    
    private fun startMeditationTimer() {
        meditationTimer = object : CountDownTimer((sessionDuration * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000).toInt()
                timerText.text = formatTime(secondsRemaining)
            }
            
            override fun onFinish() {
                endGame()
            }
        }.start()
    }
    
    private fun startBreathCycle() {
        if (!isGameRunning || isGamePaused) return
        
        currentPhase = BreathPhase.INHALE
        animateBreathPhase()
    }
    
    private fun animateBreathPhase() {
        when (currentPhase) {
            BreathPhase.INHALE -> {
                breathGuideText.text = "Breathe In..."
                breathGuideText.setTextColor(ContextCompat.getColor(this, R.color.primary))
                animateCircle(200, 300, breathInDuration)
                
                breathCycleTimer = object : CountDownTimer(breathInDuration, 100) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        currentPhase = BreathPhase.HOLD
                        animateBreathPhase()
                    }
                }.start()
            }
            BreathPhase.HOLD -> {
                breathGuideText.text = "Hold..."
                breathGuideText.setTextColor(ContextCompat.getColor(this, R.color.secondary))
                
                breathCycleTimer = object : CountDownTimer(holdDuration, 100) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        currentPhase = BreathPhase.EXHALE
                        animateBreathPhase()
                    }
                }.start()
            }
            BreathPhase.EXHALE -> {
                breathGuideText.text = "Breathe Out..."
                breathGuideText.setTextColor(ContextCompat.getColor(this, R.color.primary_light))
                animateCircle(300, 200, breathOutDuration)
                
                breathCycleTimer = object : CountDownTimer(breathOutDuration, 100) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        currentPhase = BreathPhase.PAUSE
                        animateBreathPhase()
                    }
                }.start()
            }
            BreathPhase.PAUSE -> {
                breathGuideText.text = "Relax..."
                breathGuideText.setTextColor(ContextCompat.getColor(this, R.color.text_secondary_light))
                
                breathCycleTimer = object : CountDownTimer(2000, 100) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        cyclesCompleted++
                        updateScore(10)
                        startBreathCycle()
                    }
                }.start()
            }
        }
    }
    
    private fun animateCircle(fromSize: Int, toSize: Int, duration: Long) {
        val scaleFrom = fromSize / 200f
        val scaleTo = toSize / 200f
        
        ObjectAnimator.ofFloat(meditationCircle, "scaleX", scaleFrom, scaleTo).apply {
            this.duration = duration
            start()
        }
        
        ObjectAnimator.ofFloat(meditationCircle, "scaleY", scaleFrom, scaleTo).apply {
            this.duration = duration
            start()
        }
        
        ObjectAnimator.ofFloat(meditationCircle, "alpha", 0.5f, 0.9f, 0.5f).apply {
            this.duration = duration
            start()
        }
    }
    
    override fun pauseGame() {
        isGamePaused = true
        pauseButton.text = getString(R.string.resume)
        meditationTimer?.cancel()
        breathCycleTimer?.cancel()
        breathGuideText.text = "Paused"
    }
    
    override fun resumeGame() {
        isGamePaused = false
        pauseButton.text = getString(R.string.pause)
        
        val secondsRemaining = parseTimeToSeconds(timerText.text.toString())
        sessionDuration = secondsRemaining
        
        startMeditationTimer()
        startBreathCycle()
    }
    
    override fun endGame() {
        isGameRunning = false
        startButton.isEnabled = true
        pauseButton.isEnabled = false
        pauseButton.text = getString(R.string.pause)
        
        meditationTimer?.cancel()
        breathCycleTimer?.cancel()
        
        breathGuideText.text = "Session Complete! 🎉"
        instructionText.text = "Well done! You completed $cyclesCompleted breath cycles."
        
        // Calculate final score
        val finalScore = cyclesCompleted * 10 + 50 // Bonus for completing session
        updateScore(finalScore)
        
        // Save game result
        GameManager.saveGameScore(this, GameManager.GameScore(
            gameId = "meditation_timer",
            score = finalScore,
            maxScore = 200,
            timeSpent = 300 - parseTimeToSeconds(timerText.text.toString()),
            completedAt = System.currentTimeMillis(),
            level = 1,
            stars = if (cyclesCompleted >= 15) 3 else if (cyclesCompleted >= 10) 2 else 1
        ))
        
        // Send notification
        sendCompletionNotification(finalScore, cyclesCompleted)
        
        // Show completion dialog
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            showCompletionDialog()
        }, 1500)
    }
    
    private fun showCompletionDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🧘 Meditation Complete!")
            .setMessage("You completed $cyclesCompleted breath cycles.\n\nYou earned ${cyclesCompleted * 10 + 50} points!\n\nRegular meditation improves focus, reduces stress, and promotes overall wellness.")
            .setPositiveButton("Finish") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun sendCompletionNotification(finalScore: Int, cycles: Int) {
        val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        
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
            .setContentTitle("🧘 Meditation Complete!")
            .setContentText("Meditation Session: $cycles breath cycles - $finalScore points earned!")
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle()
                .bigText("Congratulations! You completed a guided meditation session\n\nBreath Cycles: $cycles\nPoints: $finalScore\n\nKeep practicing for inner peace! 🌟"))
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        
        notificationManager.notify(2003, notification)
    }
    
    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }
    
    private fun parseTimeToSeconds(timeString: String): Int {
        val parts = timeString.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        meditationTimer?.cancel()
        breathCycleTimer?.cancel()
    }
}




