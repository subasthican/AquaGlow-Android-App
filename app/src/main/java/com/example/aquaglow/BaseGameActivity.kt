package com.example.aquaglow

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.util.Locale

/**
 * Base class for all mini-games
 */
abstract class BaseGameActivity : AppCompatActivity() {
    
    protected lateinit var gameTitle: TextView
    protected lateinit var gameDescription: TextView
    protected lateinit var progressIndicator: CircularProgressIndicator
    protected lateinit var scoreText: TextView
    protected lateinit var timeText: TextView
    protected lateinit var startButton: MaterialButton
    protected lateinit var pauseButton: MaterialButton
    protected lateinit var backButton: MaterialButton
    
    protected var gameScore = 0
    protected var gameTime = 0
    protected var isGameRunning = false
    protected var isGamePaused = false
    
    protected lateinit var currentGame: GameManager.MiniGame
    
    // Timer for tracking game time
    private val handler = Handler(Looper.getMainLooper())
    private var timeRunnable: Runnable? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_game)
        
        initializeViews()
        setupClickListeners()
        loadGameData()
        setupGame()
    }
    
    private fun initializeViews() {
        gameTitle = findViewById(R.id.gameTitle)
        gameDescription = findViewById(R.id.gameDescription)
        progressIndicator = findViewById(R.id.progressIndicator)
        scoreText = findViewById(R.id.scoreText)
        timeText = findViewById(R.id.timeText)
        startButton = findViewById(R.id.startButton)
        pauseButton = findViewById(R.id.pauseButton)
        backButton = findViewById(R.id.backButton)
        
        // Initialize displays
        scoreText.text = "Score: 0"
        timeText.text = "00:00"
        progressIndicator.progress = 0
    }
    
    private fun setupClickListeners() {
        startButton.setOnClickListener {
            if (!isGameRunning) {
                startGame()
            }
        }
        
        pauseButton.setOnClickListener {
            if (isGameRunning && !isGamePaused) {
                pauseGame()
            } else if (isGamePaused) {
                resumeGame()
            }
        }
        
        backButton.setOnClickListener {
            finish()
        }
    }
    
    private fun loadGameData() {
        val gameId = intent.getStringExtra("game_id") ?: ""
        val allGames = GameManager.getAllGames(this)
        currentGame = allGames.find { it.id == gameId } ?: allGames.first()
        
        gameTitle.text = currentGame.title
        gameDescription.text = currentGame.description
    }
    
    protected abstract fun setupGame()
    protected abstract fun startGame()
    protected abstract fun pauseGame()
    protected abstract fun resumeGame()
    protected abstract fun endGame()
    
    /**
     * Start tracking time - call this in your startGame() implementation
     */
    protected fun startTimeTracking() {
        gameTime = 0
        isGameRunning = true
        isGamePaused = false
        pauseButton.isEnabled = true
        startButton.isEnabled = false
        
        timeRunnable = object : Runnable {
            override fun run() {
                if (isGameRunning && !isGamePaused) {
                    gameTime++
                    updateTime(gameTime)
                    handler.postDelayed(this, 1000)
                }
            }
        }
        handler.post(timeRunnable!!)
    }
    
    /**
     * Stop tracking time - call this in your endGame() implementation
     */
    protected fun stopTimeTracking() {
        isGameRunning = false
        timeRunnable?.let { handler.removeCallbacks(it) }
        pauseButton.isEnabled = false
    }
    
    /**
     * Pause time tracking
     */
    protected fun pauseTimeTracking() {
        isGamePaused = true
        pauseButton.text = "Resume"
    }
    
    /**
     * Resume time tracking
     */
    protected fun resumeTimeTracking() {
        isGamePaused = false
        pauseButton.text = "Pause"
        handler.post(timeRunnable!!)
    }
    
    protected fun updateScore(points: Int) {
        gameScore += points
        scoreText.text = "Score: $gameScore"
    }
    
    protected fun updateTime(seconds: Int) {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        timeText.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
        
        // Update progress indicator if game has a duration
        if (::currentGame.isInitialized && currentGame.duration > 0) {
            val progress = (seconds.toFloat() / currentGame.duration * 100).toInt().coerceIn(0, 100)
            progressIndicator.progress = progress
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopTimeTracking()
    }
    
    protected fun saveGameResult(maxScore: Int = currentGame.points * 10, stars: Int = calculateStars(maxScore)) {
        val score = GameManager.GameScore(
            gameId = currentGame.id,
            score = gameScore,
            maxScore = maxScore,
            timeSpent = gameTime, // Use the tracked time
            completedAt = System.currentTimeMillis(),
            level = calculateLevel(),
            stars = stars
        )
        
        GameManager.saveGameScore(this, score)
    }
    
    private fun calculateLevel(): Int {
        return (gameScore / 100) + 1
    }
    
    private fun calculateStars(maxScore: Int = currentGame.points * 10): Int {
        if (maxScore == 0) return 1
        val percentage = (gameScore.toFloat() / maxScore * 100).toInt()
        return when {
            percentage >= 90 -> 3
            percentage >= 70 -> 2
            else -> 1
        }
    }
}
