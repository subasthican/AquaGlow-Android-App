package com.example.aquaglow

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView

/**
 * Breathing Exercise Game - Guided breathing with visual cues
 */
class BreathingGameActivity : BaseGameActivity() {
    
    private lateinit var breathingCircle: View
    private lateinit var instructionText: TextView
    private lateinit var countdownText: TextView
    private lateinit var cycleCountText: TextView
    
    private var breathingAnimation: ValueAnimator? = null
    private var cycleTimer: CountDownTimer? = null
    private var isBreathingIn = true
    private var currentCycle = 0
    private val totalCycles = 5
    private val breathInDuration = 4000L // 4 seconds
    private val holdDuration = 4000L // 4 seconds
    private val breathOutDuration = 4000L // 4 seconds
    
    override fun setupGame() {
        // Create breathing circle
        breathingCircle = View(this).apply {
            layoutParams = android.widget.FrameLayout.LayoutParams(
                200,
                200
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
            background = resources.getDrawable(R.drawable.breathing_circle, null)
        }
        
        // Create instruction text
        instructionText = TextView(this).apply {
            text = "Get Ready"
            textSize = 24f
            setTextColor(resources.getColor(R.color.text_primary, null))
            gravity = android.view.Gravity.CENTER
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER
                topMargin = -200
            }
        }
        
        // Create countdown text
        countdownText = TextView(this).apply {
            text = "4"
            textSize = 48f
            setTextColor(resources.getColor(R.color.secondary, null))
            gravity = android.view.Gravity.CENTER
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
        }
        
        // Create cycle count text
        cycleCountText = TextView(this).apply {
            text = "Cycle: 0/$totalCycles"
            textSize = 18f
            setTextColor(resources.getColor(R.color.text_secondary, null))
            gravity = android.view.Gravity.CENTER
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER
                topMargin = 300
            }
        }
        
        val gameArea = findViewById<android.widget.FrameLayout>(R.id.gameArea)
        gameArea.addView(breathingCircle)
        gameArea.addView(instructionText)
        gameArea.addView(countdownText)
        gameArea.addView(cycleCountText)
    }
    
    override fun startGame() {
        currentCycle = 0
        gameScore = 0
        startTimeTracking() // Start tracking time
        startBreathingCycle()
    }
    
    private fun startBreathingCycle() {
        if (currentCycle >= totalCycles) {
            endGame()
            return
        }
        
        currentCycle++
        cycleCountText.text = "Cycle: $currentCycle/$totalCycles"
        
        // Add points for each cycle (not replace)
        updateScore(10)
        
        // Breathe in phase
        breatheIn()
    }
    
    private fun breatheIn() {
        isBreathingIn = true
        instructionText.text = "Breathe In"
        
        // Animate circle expanding
        breathingAnimation = ValueAnimator.ofInt(200, 400).apply {
            duration = breathInDuration
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                val size = animator.animatedValue as Int
                breathingCircle.layoutParams = (breathingCircle.layoutParams as android.widget.FrameLayout.LayoutParams).apply {
                    width = size
                    height = size
                }
                breathingCircle.requestLayout()
            }
            start()
        }
        
        // Countdown timer
        cycleTimer = object : CountDownTimer(breathInDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countdownText.text = "${(millisUntilFinished / 1000) + 1}"
            }
            
            override fun onFinish() {
                hold()
            }
        }.start()
    }
    
    private fun hold() {
        instructionText.text = "Hold"
        
        // Hold countdown
        cycleTimer = object : CountDownTimer(holdDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countdownText.text = "${(millisUntilFinished / 1000) + 1}"
            }
            
            override fun onFinish() {
                breatheOut()
            }
        }.start()
    }
    
    private fun breatheOut() {
        isBreathingIn = false
        instructionText.text = "Breathe Out"
        
        // Animate circle shrinking
        breathingAnimation = ValueAnimator.ofInt(400, 200).apply {
            duration = breathOutDuration
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                val size = animator.animatedValue as Int
                breathingCircle.layoutParams = (breathingCircle.layoutParams as android.widget.FrameLayout.LayoutParams).apply {
                    width = size
                    height = size
                }
                breathingCircle.requestLayout()
            }
            start()
        }
        
        // Countdown timer
        cycleTimer = object : CountDownTimer(breathOutDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countdownText.text = "${(millisUntilFinished / 1000) + 1}"
            }
            
            override fun onFinish() {
                startBreathingCycle()
            }
        }.start()
    }
    
    override fun pauseGame() {
        breathingAnimation?.pause()
        cycleTimer?.cancel()
        pauseTimeTracking()
    }
    
    override fun resumeGame() {
        breathingAnimation?.resume()
        resumeTimeTracking()
    }
    
    override fun endGame() {
        breathingAnimation?.cancel()
        cycleTimer?.cancel()
        stopTimeTracking()
        
        instructionText.text = "Complete! 🎉"
        countdownText.text = "Well Done!"
        
        // Score is already calculated from individual cycles
        // Save game result using base class method
        val maxScore = totalCycles * 10
        val stars = if (currentCycle >= totalCycles) 3 else 2
        saveGameResult(maxScore = maxScore, stars = stars)
        
        // Send completion notification
        sendCompletionNotification(gameScore, currentCycle)
        
        // Show completion dialog after 2 seconds
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            showCompletionDialog()
        }, 2000)
    }
    
    private fun showCompletionDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🎉 Breathing Exercise Complete!")
            .setMessage("You completed $currentCycle breathing cycles.\n\nYou earned ${currentCycle * 10} points!")
            .setPositiveButton("Finish") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun sendCompletionNotification(finalScore: Int, cycles: Int) {
        val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        
        // Create notification channel
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
        
        // Create notification
        val notification = androidx.core.app.NotificationCompat.Builder(this, "game_completion")
            .setSmallIcon(R.drawable.ic_trending_up)
            .setContentTitle("🎉 Game Completed!")
            .setContentText("Breathing Exercise: $cycles cycles completed - $finalScore points earned!")
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle()
                .bigText("Congratulations! You completed the Breathing Exercise\n\nCycles: $cycles\nPoints: $finalScore\n\nGreat job on your wellness journey! 🌟"))
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        
        notificationManager.notify(2001, notification)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        breathingAnimation?.cancel()
        cycleTimer?.cancel()
    }
}

