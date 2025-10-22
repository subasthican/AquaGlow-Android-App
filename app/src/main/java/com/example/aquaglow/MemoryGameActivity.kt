package com.example.aquaglow

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.animation.doOnEnd
import kotlin.random.Random

/**
 * Memory Sequence Game - Remember and repeat the color sequence
 */
class MemoryGameActivity : BaseGameActivity() {
    
    private lateinit var gameGrid: GridLayout
    private lateinit var instructionText: TextView
    private lateinit var sequenceText: TextView
    
    private var gameTimer: CountDownTimer? = null
    private var sequence = mutableListOf<Int>()
    private var userSequence = mutableListOf<Int>()
    private var currentSequenceIndex = 0
    private var isShowingSequence = false
    private var sequenceLength = 3
    private var correctSequences = 0
    
    private val colors = listOf(
        R.color.primary,
        R.color.secondary,
        R.color.primary_light,
        R.color.secondary_light,
        R.color.accent,
        R.color.accent_light
    )
    
    private val colorButtons = mutableListOf<View>()
    
    override fun setupGame() {
        // Create game grid
        gameGrid = GridLayout(this).apply {
            columnCount = 3
            rowCount = 2
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            ).apply {
                setMargins(32, 32, 32, 32)
            }
        }
        
        // Create instruction text
        instructionText = TextView(this).apply {
            text = "Watch the sequence, then repeat it!"
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
        
        // Create sequence text
        sequenceText = TextView(this).apply {
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
        
        // Use the base class scoreText - no need to create our own
        
        // Create color buttons
        createColorButtons()
        
        // Add views to game area
        findViewById<android.widget.FrameLayout>(R.id.gameArea).apply {
            addView(instructionText)
            addView(sequenceText)
            addView(gameGrid)
        }
    }
    
    private fun createColorButtons() {
        colorButtons.clear()
        gameGrid.removeAllViews()
        
        for (i in 0 until 6) {
            val button = View(this).apply {
                setBackgroundColor(resources.getColor(colors[i], null))
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 0
                    columnSpec = GridLayout.spec(i % 3, 1f)
                    rowSpec = GridLayout.spec(i / 3, 1f)
                    setMargins(12, 12, 12, 12) // Larger margins for better visibility
                }
                setOnClickListener { onColorButtonClick(i) }
                // Add elevation for better visibility
                elevation = 4f
            }
            colorButtons.add(button)
            gameGrid.addView(button)
        }
    }
    
    override fun startGame() {
        isGameRunning = true
        startButton.isEnabled = false
        pauseButton.isEnabled = true
        instructionText.text = getString(R.string.game_watch_sequence)
        
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
        pauseButton.text = getString(R.string.resume)
        gameTimer?.cancel()
    }
    
    override fun resumeGame() {
        isGamePaused = false
        pauseButton.text = getString(R.string.pause)
        
        // Resume timer
        gameTimer = object : CountDownTimer(gameTime * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateTime((millisUntilFinished / 1000).toInt())
            }
            
            override fun onFinish() {
                endGame()
            }
        }.start()
    }
    
    override fun endGame() {
        isGameRunning = false
        startButton.isEnabled = true
        pauseButton.isEnabled = false
        pauseButton.text = getString(R.string.pause)
        
        gameTimer?.cancel()
        
        // Score is already calculated from individual sequences
        // Show completion message
        instructionText.text = String.format(getString(R.string.game_over), correctSequences)
        sequenceText.text = String.format(getString(R.string.game_final_score), gameScore)
        
        // Save game result
        saveGameResult()
    }
    
    private fun startNewSequence() {
        if (!isGameRunning || isGamePaused) return
        
        sequence.clear()
        userSequence.clear()
        currentSequenceIndex = 0
        
        // Generate random sequence
        for (i in 0 until sequenceLength) {
            sequence.add(Random.nextInt(6))
        }
        
        sequenceText.text = String.format(getString(R.string.game_watch_colors), sequenceLength)
        showSequence()
    }
    
    private fun showSequence() {
        isShowingSequence = true
        disableButtons()
        
        var delay = 0L
        sequence.forEachIndexed { index, colorIndex ->
            Handler(Looper.getMainLooper()).postDelayed({
                if (isGameRunning && !isGamePaused) {
                    highlightButton(colorIndex) {
                        if (index == sequence.size - 1) {
                            // Sequence complete, enable user input
                            Handler(Looper.getMainLooper()).postDelayed({
                                isShowingSequence = false
                                enableButtons()
                                sequenceText.text = getString(R.string.game_repeat_sequence)
                            }, 1000) // Longer delay before user input
                        }
                    }
                }
            }, delay)
            delay += 1500L // Slower timing between buttons (1.5 seconds)
        }
    }
    
    private fun highlightButton(colorIndex: Int, onComplete: () -> Unit) {
        val button = colorButtons[colorIndex]
        val originalColor = button.background
        
        // More visible highlight animation
        val scaleAnimator = ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.2f, 1f)
        val scaleYAnimator = ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.2f, 1f)
        val alphaAnimator = ObjectAnimator.ofFloat(button, "alpha", 1f, 0.5f, 1f)
        
        val animatorSet = AnimatorSet().apply {
            playTogether(scaleAnimator, scaleYAnimator, alphaAnimator)
            duration = 800
            doOnEnd { onComplete() }
        }
        animatorSet.start()
    }
    
    private fun onColorButtonClick(colorIndex: Int) {
        if (isShowingSequence || !isGameRunning || isGamePaused) return
        
        userSequence.add(colorIndex)
        
        // Visual feedback for button click
        val button = colorButtons[colorIndex]
        val scaleAnimator = ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.3f, 1f)
        val scaleYAnimator = ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.3f, 1f)
        val animatorSet = AnimatorSet().apply {
            playTogether(scaleAnimator, scaleYAnimator)
            duration = 200
        }
        animatorSet.start()
        
        // Show progress
        sequenceText.text = "Progress: ${userSequence.size}/${sequence.size}"
        
        if (userSequence.size == sequence.size) {
            // Check if sequence is correct
            if (userSequence == sequence) {
                // Correct sequence
                correctSequences++
                val points = sequenceLength * 10 + correctSequences * 5
                updateScore(points) // This will update scoreText automatically
                
                sequenceText.text = "✅ Correct! +${points} pts (Sequences: ${correctSequences})"
                Handler(Looper.getMainLooper()).postDelayed({
                    sequenceLength++
                    startNewSequence()
                }, 1500)
            } else {
                // Wrong sequence - deduct points
                val penalty = 5
                if (gameScore >= penalty) {
                    updateScore(-penalty) // This will update scoreText automatically
                }
                sequenceText.text = "❌ Wrong! -${penalty} pts"
                Handler(Looper.getMainLooper()).postDelayed({
                    startNewSequence()
                }, 1500)
            }
        }
    }
    
    private fun enableButtons() {
        colorButtons.forEach { it.isEnabled = true }
    }
    
    private fun disableButtons() {
        colorButtons.forEach { it.isEnabled = false }
    }
}
