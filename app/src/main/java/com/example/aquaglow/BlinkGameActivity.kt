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
 * Blink Game - Focus and attention training
 */
class BlinkGameActivity : BaseGameActivity() {
    
    private lateinit var blinkButton: View
    private lateinit var instructionText: TextView
    private lateinit var feedbackText: TextView
    private lateinit var sequenceText: TextView
    
    private var gameTimer: CountDownTimer? = null
    private var blinkTimer: CountDownTimer? = null
    
    private var currentSequence = mutableListOf<Int>()
    private var playerSequence = mutableListOf<Int>()
    private var currentStep = 0
    private var sequenceLength = 3
    private var isShowingSequence = false
    private var isWaitingForInput = false
    private var correctSequences = 0
    private var wrongSequences = 0
    
    override fun setupGame() {
        // Create blink button
        blinkButton = View(this).apply {
            setBackgroundResource(R.drawable.blink_button)
            layoutParams = android.widget.FrameLayout.LayoutParams(200, 200).apply {
                gravity = android.view.Gravity.CENTER
            }
            visibility = View.GONE
            setOnClickListener { onBlinkButtonClick() }
        }
        
        // Create instruction text
        instructionText = TextView(this).apply {
            text = "Watch the sequence and repeat it!"
            textSize = 18f
            setTextColor(resources.getColor(R.color.text_primary_light, null))
            gravity = android.view.Gravity.CENTER
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(32, 0, 32, 0)
            }
        }
        
        // Create feedback text
        feedbackText = TextView(this).apply {
            text = ""
            textSize = 16f
            setTextColor(resources.getColor(R.color.primary, null))
            gravity = android.view.Gravity.CENTER
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 50
            }
        }
        
        // Create sequence text
        sequenceText = TextView(this).apply {
            text = ""
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
            addView(feedbackText)
            addView(sequenceText)
            addView(blinkButton)
        }
    }
    
    override fun startGame() {
        isGameRunning = true
        startButton.isEnabled = false
        pauseButton.isEnabled = true
        instructionText.text = "Get ready! Watch the sequence..."
        
        // Reset stats
        correctSequences = 0
        wrongSequences = 0
        sequenceLength = 3
        
        // Start game timer
        gameTimer = object : CountDownTimer(currentGame.duration * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateTime((millisUntilFinished / 1000).toInt())
            }
            
            override fun onFinish() {
                endGame()
            }
        }.start()
        
        // Start first sequence
        startNewSequence()
    }
    
    override fun pauseGame() {
        isGamePaused = true
        pauseButton.text = "Resume"
        gameTimer?.cancel()
        blinkTimer?.cancel()
        hideBlinkButton()
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
        
        // Resume sequence
        if (isWaitingForInput) {
            showBlinkButton()
        } else {
            startNewSequence()
        }
    }
    
    override fun endGame() {
        isGameRunning = false
        startButton.isEnabled = true
        pauseButton.isEnabled = false
        pauseButton.text = "Pause"
        
        gameTimer?.cancel()
        blinkTimer?.cancel()
        hideBlinkButton()
        
        // Show final results
        instructionText.text = "Game Over! Final Results:"
        feedbackText.text = "Correct: $correctSequences | Wrong: $wrongSequences"
        sequenceText.text = "Total Score: $gameScore points"
        
        // Save game result
        saveGameResult()
    }
    
    private fun startNewSequence() {
        if (!isGameRunning || isGamePaused) return
        
        currentSequence.clear()
        playerSequence.clear()
        currentStep = 0
        isShowingSequence = true
        isWaitingForInput = false
        
        // Generate random sequence
        for (i in 0 until sequenceLength) {
            currentSequence.add(Random.nextInt(1, 5)) // 1-4 blinks
        }
        
        instructionText.text = "Watch the sequence..."
        feedbackText.text = ""
        sequenceText.text = "Sequence $correctSequences + 1"
        
        // Show sequence
        showSequence()
    }
    
    private fun showSequence() {
        if (currentStep >= currentSequence.size) {
            // Sequence complete, wait for player input
            isShowingSequence = false
            isWaitingForInput = true
            instructionText.text = "Now repeat the sequence!"
            showBlinkButton()
            return
        }
        
        val blinks = currentSequence[currentStep]
        showBlinkButton()
        
        // Blink the button
        blinkTimer = object : CountDownTimer(blinks * 500L, 100) {
            var currentBlink = 0
            
            override fun onTick(millisUntilFinished: Long) {
                if (millisUntilFinished % 500L < 250L) {
                    blinkButton.visibility = View.VISIBLE
                } else {
                    blinkButton.visibility = View.GONE
                }
            }
            
            override fun onFinish() {
                blinkButton.visibility = View.GONE
                currentStep++
                
                // Wait before next blink in sequence
                Handler(Looper.getMainLooper()).postDelayed({
                    showSequence()
                }, 300)
            }
        }.start()
    }
    
    private fun showBlinkButton() {
        blinkButton.visibility = View.VISIBLE
        blinkButton.alpha = 1f
        blinkButton.scaleX = 1f
        blinkButton.scaleY = 1f
    }
    
    private fun hideBlinkButton() {
        blinkButton.visibility = View.GONE
    }
    
    private fun onBlinkButtonClick() {
        if (!isWaitingForInput || !isGameRunning || isGamePaused) return
        
        // Count the blinks (simplified - just add to sequence)
        playerSequence.add(1) // Each click = 1 blink
        
        // Show visual feedback
        ObjectAnimator.ofFloat(blinkButton, "scaleX", 1f, 1.2f, 1f).setDuration(200).start()
        ObjectAnimator.ofFloat(blinkButton, "scaleY", 1f, 1.2f, 1f).setDuration(200).start()
        
        // Check if sequence is complete
        if (playerSequence.size >= currentSequence.size) {
            checkSequence()
        }
    }
    
    private fun checkSequence() {
        isWaitingForInput = false
        hideBlinkButton()
        
        // Check if sequence is correct
        val isCorrect = playerSequence.size == currentSequence.size && 
                       playerSequence.zip(currentSequence).all { it.first == it.second }
        
        if (isCorrect) {
            correctSequences++
            val points = sequenceLength * 10 + correctSequences * 5
            updateScore(points)
            feedbackText.text = "Correct! +${points} pts"
            feedbackText.setTextColor(resources.getColor(R.color.success, null))
            
            // Increase difficulty
            if (correctSequences % 3 == 0) {
                sequenceLength++
            }
        } else {
            wrongSequences++
            val penalty = 5
            if (gameScore >= penalty) {
                updateScore(-penalty)
            }
            feedbackText.text = "Wrong! -${penalty} pts"
            feedbackText.setTextColor(resources.getColor(R.color.error, null))
        }
        
        // Start next sequence after delay
        Handler(Looper.getMainLooper()).postDelayed({
            startNewSequence()
        }, 1500)
    }
}


