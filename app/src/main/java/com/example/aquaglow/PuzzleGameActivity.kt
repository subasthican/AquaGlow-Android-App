package com.example.aquaglow

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import kotlin.random.Random

/**
 * Puzzle Slider Game - Classic 15-puzzle sliding tile game
 */
class PuzzleGameActivity : BaseGameActivity() {
    
    private lateinit var puzzleGrid: GridLayout
    private lateinit var instructionText: TextView
    private lateinit var movesText: TextView
    private lateinit var timerText: TextView
    
    private var gameTimer: CountDownTimer? = null
    private val gridSize = 3 // 3x3 grid (8-puzzle for easier gameplay)
    private val tiles = mutableListOf<PuzzleTile>()
    private var emptyTileIndex = gridSize * gridSize - 1
    private var moveCount = 0
    private var timeElapsed = 0
    
    data class PuzzleTile(
        val value: Int,
        val card: MaterialCardView,
        val textView: TextView,
        var position: Int
    )
    
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
            setPadding(16, 16, 16, 16)
        }
        
        // Instruction text
        instructionText = TextView(this).apply {
            text = "🧩 Arrange numbers in order!\nTap tiles to slide them."
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, R.color.text_primary_light))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        
        // Info layout
        val infoLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        
        movesText = TextView(this).apply {
            text = "Moves: 0"
            textSize = 18f
            setTextColor(ContextCompat.getColor(context, R.color.primary))
            setPadding(20, 0, 20, 0)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        
        timerText = TextView(this).apply {
            text = "Time: 00:00"
            textSize = 18f
            setTextColor(ContextCompat.getColor(context, R.color.secondary))
            setPadding(20, 0, 20, 0)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        
        infoLayout.addView(movesText)
        infoLayout.addView(timerText)
        
        // Puzzle grid
        puzzleGrid = GridLayout(this).apply {
            columnCount = gridSize
            rowCount = gridSize
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        // Create tiles
        createTiles()
        
        mainLayout.addView(instructionText)
        mainLayout.addView(infoLayout)
        mainLayout.addView(puzzleGrid)
        
        gameArea.addView(mainLayout)
    }
    
    private fun createTiles() {
        tiles.clear()
        puzzleGrid.removeAllViews()
        
        val tileSize = 90 // dp
        val tileSizePx = (tileSize * resources.displayMetrics.density).toInt()
        
        for (i in 0 until gridSize * gridSize) {
            val value = i + 1
            
            val tileCard = MaterialCardView(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = tileSizePx
                    height = tileSizePx
                    setMargins(8, 8, 8, 8)
                }
                radius = 12f
                cardElevation = 4f
                setCardBackgroundColor(
                    if (i == gridSize * gridSize - 1) 
                        Color.TRANSPARENT 
                    else 
                        ContextCompat.getColor(context, R.color.primary)
                )
            }
            
            val tileText = TextView(this).apply {
                text = if (i == gridSize * gridSize - 1) "" else value.toString()
                textSize = 32f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
            
            tileCard.addView(tileText)
            
            val tile = PuzzleTile(value, tileCard, tileText, i)
            tiles.add(tile)
            
            tileCard.setOnClickListener {
                if (isGameRunning && !isGamePaused) {
                    onTileClick(tile)
                }
            }
            
            puzzleGrid.addView(tileCard)
        }
    }
    
    override fun startGame() {
        isGameRunning = true
        startButton.isEnabled = false
        pauseButton.isEnabled = true
        
        moveCount = 0
        timeElapsed = 0
        movesText.text = "Moves: 0"
        
        instructionText.text = "Solve the puzzle as fast as you can!"
        
        // Shuffle puzzle
        shufflePuzzle()
        
        // Start timer
        startTimer()
    }
    
    private fun shufflePuzzle() {
        // Shuffle with valid moves to ensure solvability
        val shuffleMoves = 50
        repeat(shuffleMoves) {
            val validMoves = getValidMoves(emptyTileIndex)
            if (validMoves.isNotEmpty()) {
                val randomMove = validMoves.random()
                swapTiles(emptyTileIndex, randomMove, animate = false)
            }
        }
    }
    
    private fun getValidMoves(position: Int): List<Int> {
        val moves = mutableListOf<Int>()
        val row = position / gridSize
        val col = position % gridSize
        
        // Up
        if (row > 0) moves.add(position - gridSize)
        // Down
        if (row < gridSize - 1) moves.add(position + gridSize)
        // Left
        if (col > 0) moves.add(position - 1)
        // Right
        if (col < gridSize - 1) moves.add(position + 1)
        
        return moves
    }
    
    private fun onTileClick(tile: PuzzleTile) {
        val validMoves = getValidMoves(emptyTileIndex)
        
        if (tile.position in validMoves) {
            swapTiles(emptyTileIndex, tile.position, animate = true)
            moveCount++
            movesText.text = "Moves: $moveCount"
            updateScore(5)
            
            // Check if solved
            if (isPuzzleSolved()) {
                endGame()
            }
        }
    }
    
    private fun swapTiles(pos1: Int, pos2: Int, animate: Boolean) {
        val tile1 = tiles.find { it.position == pos1 } ?: return
        val tile2 = tiles.find { it.position == pos2 } ?: return
        
        // Swap positions
        tile1.position = pos2
        tile2.position = pos1
        
        // Update empty tile index
        if (tile1.value == gridSize * gridSize) {
            emptyTileIndex = pos2
        } else if (tile2.value == gridSize * gridSize) {
            emptyTileIndex = pos1
        }
        
        // Update UI
        puzzleGrid.removeAllViews()
        
        val sortedTiles = tiles.sortedBy { it.position }
        sortedTiles.forEach { tile ->
            puzzleGrid.addView(tile.card)
        }
        
        if (animate) {
            tile2.card.animate()
                .alpha(0.5f)
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction {
                    tile2.card.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                }
                .start()
        }
    }
    
    private fun isPuzzleSolved(): Boolean {
        return tiles.all { it.value == it.position + 1 }
    }
    
    private fun startTimer() {
        gameTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeElapsed++
                val minutes = timeElapsed / 60
                val seconds = timeElapsed % 60
                timerText.text = String.format("Time: %02d:%02d", minutes, seconds)
            }
            
            override fun onFinish() {}
        }.start()
    }
    
    override fun pauseGame() {
        isGamePaused = true
        pauseButton.text = getString(R.string.resume)
        gameTimer?.cancel()
        instructionText.text = "Game Paused"
    }
    
    override fun resumeGame() {
        isGamePaused = false
        pauseButton.text = getString(R.string.pause)
        instructionText.text = "Keep solving!"
        startTimer()
    }
    
    override fun endGame() {
        isGameRunning = false
        startButton.isEnabled = true
        pauseButton.isEnabled = false
        pauseButton.text = getString(R.string.pause)
        
        gameTimer?.cancel()
        
        instructionText.text = "🎉 Puzzle Solved!"
        
        // Calculate score (fewer moves and less time = higher score)
        val moveBonus = maxOf(0, 100 - moveCount * 2)
        val timeBonus = maxOf(0, 50 - timeElapsed)
        val finalScore = moveBonus + timeBonus + 50
        
        updateScore(finalScore)
        
        // Save game result
        GameManager.saveGameScore(this, GameManager.GameScore(
            gameId = "puzzle_slider",
            score = finalScore,
            maxScore = 200,
            timeSpent = timeElapsed,
            completedAt = System.currentTimeMillis(),
            level = 1,
            stars = if (moveCount < 30) 3 else if (moveCount < 50) 2 else 1
        ))
        
        // Send notification
        sendCompletionNotification(finalScore, moveCount)
        
        // Show completion dialog
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            showCompletionDialog()
        }, 1000)
    }
    
    private fun showCompletionDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🧩 Puzzle Solved!")
            .setMessage("Congratulations!\n\nMoves: $moveCount\nTime: ${formatTime(timeElapsed)}\nScore: $gameScore\n\nPuzzles improve problem-solving and spatial reasoning!")
            .setPositiveButton("Finish") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun sendCompletionNotification(finalScore: Int, moves: Int) {
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
            .setContentTitle("🧩 Puzzle Solved!")
            .setContentText("Puzzle Slider: $moves moves in ${formatTime(timeElapsed)} - $finalScore points!")
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle()
                .bigText("Congratulations! You solved the puzzle\n\nMoves: $moves\nTime: ${formatTime(timeElapsed)}\nScore: $finalScore\n\nGreat problem-solving! 🌟"))
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        
        notificationManager.notify(2004, notification)
    }
    
    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        gameTimer?.cancel()
    }
}




