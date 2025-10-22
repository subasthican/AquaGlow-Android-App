package com.example.aquaglow

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import kotlin.random.Random

/**
 * Reflex Tap Game - Quick reaction time challenge
 */
class ReflexGameActivity : BaseGameActivity() {
    
    private lateinit var targetButton: View
    private lateinit var instructionText: TextView
    private lateinit var reactionText: TextView
    private lateinit var accuracyText: TextView
    
    private var gameTimer: CountDownTimer? = null
    private var targetTimer: CountDownTimer? = null
    private var targetAppearTimer: CountDownTimer? = null
    
    private var totalTargets = 0
    private var hitTargets = 0
    private var missedTargets = 0
    private var totalReactionTime = 0L
    private var targetStartTime = 0L
    private var isTargetVisible = false
    private var currentTargetSize = 120
    private var currentTargetDuration = 2000L
    
    override fun setupGame() {
        // Create target button
        targetButton = View(this).apply {
            setBackgroundResource(R.drawable.target_button)
            layoutParams = android.widget.FrameLayout.LayoutParams(
                currentTargetSize, currentTargetSize
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
            visibility = View.GONE
            setOnClickListener { onTargetHit() }
        }
        
        // Create instruction text
        instructionText = TextView(this).apply {
            text = "Tap the targets as quickly as possible!"
            textSize = 16f
            setTextColor(resources.getColor(R.color.text_primary_light, null))
            gravity = android.view.Gravity.CENTER
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(32, 0, 32, 0)
            }
        }
        
        // Create reaction text
        reactionText = TextView(this).apply {
            text = ""
            textSize = 18f
            setTextColor(resources.getColor(R.color.primary, null))
            gravity = android.view.Gravity.CENTER
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 50
            }
        }
        
        // Create accuracy text
        accuracyText = TextView(this).apply {
            text = "Accuracy: 0% | Targets: 0"
            textSize = 14f
            setTextColor(resources.getColor(R.color.secondary, null))
            gravity = android.view.Gravity.CENTER
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 20
            }
        }
        
        // Add views to game area
        findViewById<android.widget.FrameLayout>(R.id.gameArea).apply {
            addView(instructionText)
            addView(reactionText)
            addView(accuracyText)
            addView(targetButton)
        }
    }
    
    override fun startGame() {
        isGameRunning = true
        startButton.isEnabled = false
        pauseButton.isEnabled = true
        instructionText.text = "Get ready! Targets will appear randomly..."
        
        // Reset stats
        totalTargets = 0
        hitTargets = 0
        missedTargets = 0
        totalReactionTime = 0L
        
        // Start game timer
        gameTimer = object : CountDownTimer(currentGame.duration * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateTime((millisUntilFinished / 1000).toInt())
            }
            
            override fun onFinish() {
                endGame()
            }
        }.start()
        
        // Start spawning targets
        spawnNextTarget()
    }
    
    override fun pauseGame() {
        isGamePaused = true
        pauseButton.text = "Resume"
        gameTimer?.cancel()
        targetTimer?.cancel()
        targetAppearTimer?.cancel()
        hideTarget()
    }
    
    override fun resumeGame() {
        isGamePaused = false
        pauseButton.text = "Pause"
        
        // Resume timer
        gameTimer = object : CountDownTimer(gameTime * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateTime((millisUntilFinished / 1000).toInt())
            }
            
            override fun onFinish() {
                endGame()
            }
        }.start()
        
        // Resume spawning targets
        spawnNextTarget()
    }
    
    override fun endGame() {
        isGameRunning = false
        startButton.isEnabled = true
        pauseButton.isEnabled = false
        pauseButton.text = "Pause"
        
        gameTimer?.cancel()
        targetTimer?.cancel()
        targetAppearTimer?.cancel()
        hideTarget()
        
        // Calculate final stats
        val accuracy = if (totalTargets > 0) (hitTargets * 100) / totalTargets else 0
        val averageReactionTime = if (hitTargets > 0) totalReactionTime / hitTargets else 0
        
        // Show completion message
        instructionText.text = "Game Over! Final Results:"
        reactionText.text = "Hit: $hitTargets | Missed: $missedTargets"
        accuracyText.text = "Accuracy: $accuracy% | Avg Reaction: ${averageReactionTime}ms"
        
        // Save game result (score is already calculated from individual hits)
        saveGameResult()
    }
    
    private fun spawnNextTarget() {
        if (!isGameRunning || isGamePaused) return
        
        // Random delay before target appears (1-3 seconds)
        val delay = Random.nextLong(1000, 3000)
        
        targetAppearTimer = object : CountDownTimer(delay, 100) {
            override fun onTick(millisUntilFinished: Long) {
                // Countdown to target appearance
            }
            
            override fun onFinish() {
                showTarget()
            }
        }.start()
    }
    
    private fun showTarget() {
        if (!isGameRunning || isGamePaused) return
        
        isTargetVisible = true
        totalTargets++
        
        // Random position for target
        val maxX = findViewById<android.widget.FrameLayout>(R.id.gameArea).width - currentTargetSize
        val maxY = findViewById<android.widget.FrameLayout>(R.id.gameArea).height - currentTargetSize
        
        val x = Random.nextInt(0, maxX.coerceAtLeast(0))
        val y = Random.nextInt(0, maxY.coerceAtLeast(0))
        
        targetButton.layoutParams = android.widget.FrameLayout.LayoutParams(
            currentTargetSize, currentTargetSize
        ).apply {
            leftMargin = x
            topMargin = y
        }
        
        targetButton.visibility = View.VISIBLE
        targetStartTime = System.currentTimeMillis()
        
        // Animate target appearance
        targetButton.alpha = 0f
        targetButton.scaleX = 0.5f
        targetButton.scaleY = 0.5f
        
        ObjectAnimator.ofFloat(targetButton, "alpha", 0f, 1f).setDuration(200).start()
        ObjectAnimator.ofFloat(targetButton, "scaleX", 0.5f, 1f).setDuration(200).start()
        ObjectAnimator.ofFloat(targetButton, "scaleY", 0.5f, 1f).setDuration(200).start()
        
        // Target disappears after duration
        targetTimer = object : CountDownTimer(currentTargetDuration, 100) {
            override fun onTick(millisUntilFinished: Long) {
                // Target is visible
            }
            
            override fun onFinish() {
                if (isTargetVisible) {
                    onTargetMissed()
                }
            }
        }.start()
    }
    
    private fun hideTarget() {
        isTargetVisible = false
        targetButton.visibility = View.GONE
        targetTimer?.cancel()
    }
    
    private fun onTargetHit() {
        if (!isTargetVisible || !isGameRunning || isGamePaused) return
        
        val reactionTime = System.currentTimeMillis() - targetStartTime
        totalReactionTime += reactionTime
        hitTargets++
        
        // Calculate points based on reaction time and difficulty
        val points = calculateHitPoints(reactionTime)
        updateScore(points) // Add points immediately when target is hit
        
        // Show reaction time and points
        reactionText.text = "Hit! +${points} pts (${reactionTime}ms)"
        
        // Animate hit
        ObjectAnimator.ofFloat(targetButton, "scaleX", 1f, 1.2f, 0f).setDuration(300).start()
        ObjectAnimator.ofFloat(targetButton, "scaleY", 1f, 1.2f, 0f).setDuration(300).start()
        
        hideTarget()
        updateAccuracy()
        
        // Increase difficulty
        if (hitTargets % 5 == 0) {
            currentTargetSize = (currentTargetSize * 0.9).toInt().coerceAtLeast(80)
            currentTargetDuration = (currentTargetDuration * 0.9).toLong().coerceAtLeast(1000)
        }
        
        // Spawn next target
        Handler(Looper.getMainLooper()).postDelayed({
            spawnNextTarget()
        }, 500)
    }
    
    private fun calculateHitPoints(reactionTime: Long): Int {
        // Faster reaction = more points
        // Base points: 10, bonus for speed
        val basePoints = 10
        val speedBonus = when {
            reactionTime < 200 -> 20  // Very fast
            reactionTime < 400 -> 15  // Fast
            reactionTime < 600 -> 10  // Good
            reactionTime < 800 -> 5   // Average
            else -> 0                 // Slow
        }
        return basePoints + speedBonus
    }
    
    private fun onTargetMissed() {
        missedTargets++
        
        // Deduct points for missing (but don't go below 0)
        val penalty = 5
        if (gameScore >= penalty) {
            updateScore(-penalty)
        }
        
        // Show miss feedback
        reactionText.text = "Missed! -${penalty} pts"
        
        hideTarget()
        updateAccuracy()
        
        // Spawn next target
        Handler(Looper.getMainLooper()).postDelayed({
            spawnNextTarget()
        }, 500)
    }
    
    private fun updateAccuracy() {
        val accuracy = if (totalTargets > 0) (hitTargets * 100) / totalTargets else 0
        accuracyText.text = "Accuracy: $accuracy% | Targets: $totalTargets"
    }
}





