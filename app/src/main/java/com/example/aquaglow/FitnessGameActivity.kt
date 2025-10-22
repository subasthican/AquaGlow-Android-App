package com.example.aquaglow

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton

/**
 * Fitness Challenge Game - Quick exercise counter
 */
class FitnessGameActivity : BaseGameActivity() {
    
    private lateinit var exerciseNameText: TextView
    private lateinit var repsCountText: TextView
    private lateinit var repsTargetText: TextView
    private lateinit var timerText: TextView
    private lateinit var incrementButton: MaterialButton
    private lateinit var nextExerciseButton: MaterialButton
    private lateinit var motivationText: TextView
    
    private var currentExerciseIndex = 0
    private var currentReps = 0
    private var totalReps = 0
    private var gameTimer: CountDownTimer? = null
    private var timeRemaining = 120 // 2 minutes
    
    private val exercises = listOf(
        Exercise("Jumping Jacks", 10, "💪"),
        Exercise("Squats", 10, "🦵"),
        Exercise("Push-ups", 5, "💪"),
        Exercise("High Knees", 15, "🏃"),
        Exercise("Arm Circles", 10, "🙆")
    )
    
    data class Exercise(val name: String, val targetReps: Int, val emoji: String)
    
    override fun setupGame() {
        val gameArea = findViewById<android.widget.FrameLayout>(R.id.gameArea)
        
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(40, 40, 40, 40)
        }
        
        // Timer
        timerText = TextView(this).apply {
            text = "Time: 2:00"
            textSize = 18f
            setTextColor(resources.getColor(R.color.text_secondary, null))
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 40
            }
        }
        
        // Exercise Name
        exerciseNameText = TextView(this).apply {
            text = "💪 Jumping Jacks"
            textSize = 32f
            setTextColor(resources.getColor(R.color.text_primary, null))
            gravity = android.view.Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 20
            }
        }
        
        // Reps Counter
        repsCountText = TextView(this).apply {
            text = "0"
            textSize = 72f
            setTextColor(resources.getColor(R.color.secondary, null))
            gravity = android.view.Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 10
            }
        }
        
        // Target Reps
        repsTargetText = TextView(this).apply {
            text = "/ 10 reps"
            textSize = 24f
            setTextColor(resources.getColor(R.color.text_secondary, null))
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 40
            }
        }
        
        // Increment Button
        incrementButton = MaterialButton(this).apply {
            text = "Complete Rep ✓"
            textSize = 20f
            setTextColor(resources.getColor(R.color.white, null))
            backgroundTintList = android.content.res.ColorStateList.valueOf(
                resources.getColor(R.color.primary, null)
            )
            cornerRadius = 24
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                200
            ).apply {
                bottomMargin = 20
            }
            setOnClickListener {
                incrementReps()
            }
        }
        
        // Next Exercise Button
        nextExerciseButton = MaterialButton(this).apply {
            text = "Next Exercise →"
            textSize = 16f
            setTextColor(resources.getColor(R.color.white, null))
            backgroundTintList = android.content.res.ColorStateList.valueOf(
                resources.getColor(R.color.secondary, null)
            )
            cornerRadius = 24
            visibility = android.view.View.GONE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 20
            }
            setOnClickListener {
                nextExercise()
            }
        }
        
        // Motivation Text
        motivationText = TextView(this).apply {
            text = "You can do it! 💪"
            textSize = 16f
            setTextColor(resources.getColor(R.color.text_secondary, null))
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        container.addView(timerText)
        container.addView(exerciseNameText)
        container.addView(repsCountText)
        container.addView(repsTargetText)
        container.addView(incrementButton)
        container.addView(nextExerciseButton)
        container.addView(motivationText)
        
        gameArea.addView(container)
    }
    
    override fun startGame() {
        currentExerciseIndex = 0
        totalReps = 0
        gameScore = 0
        
        startTimeTracking() // Start tracking time
        loadExercise()
        startTimer()
    }
    
    private fun loadExercise() {
        if (currentExerciseIndex >= exercises.size) {
            endGame()
            return
        }
        
        val exercise = exercises[currentExerciseIndex]
        currentReps = 0
        
        exerciseNameText.text = "${exercise.emoji} ${exercise.name}"
        repsCountText.text = "0"
        repsTargetText.text = "/ ${exercise.targetReps} reps"
        incrementButton.visibility = android.view.View.VISIBLE
        nextExerciseButton.visibility = android.view.View.GONE
        
        updateMotivation()
    }
    
    private fun incrementReps() {
        currentReps++
        totalReps++
        repsCountText.text = "$currentReps"
        
        // Update score in real-time
        gameScore = totalReps * 5
        scoreText.text = "Score: $gameScore"
        
        val exercise = exercises[currentExerciseIndex]
        if (currentReps >= exercise.targetReps) {
            // Exercise complete
            incrementButton.visibility = android.view.View.GONE
            nextExerciseButton.visibility = android.view.View.VISIBLE
            motivationText.text = "Great job! 🎉 Rest for a moment"
        } else {
            updateMotivation()
        }
    }
    
    private fun nextExercise() {
        currentExerciseIndex++
        loadExercise()
    }
    
    private fun updateMotivation() {
        val messages = listOf(
            "Keep going! 💪",
            "You're doing great! 🌟",
            "One more rep! 🔥",
            "Push yourself! 💯",
            "Almost there! ⚡",
            "You got this! 👊",
            "Stay strong! 🦸"
        )
        motivationText.text = messages.random()
    }
    
    private fun startTimer() {
        gameTimer = object : CountDownTimer((timeRemaining * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = (millisUntilFinished / 1000).toInt()
                val minutes = timeRemaining / 60
                val seconds = timeRemaining % 60
                timerText.text = "Time: $minutes:${seconds.toString().padStart(2, '0')}"
            }
            
            override fun onFinish() {
                endGame()
            }
        }.start()
    }
    
    override fun pauseGame() {
        gameTimer?.cancel()
        pauseTimeTracking()
    }
    
    override fun resumeGame() {
        startTimer()
        resumeTimeTracking()
    }
    
    override fun endGame() {
        gameTimer?.cancel()
        stopTimeTracking()
        
        // Update final score
        val finalScore = totalReps * 5
        gameScore = finalScore
        scoreText.text = "Score: $gameScore"
        
        // Save game result using base class method
        val maxScore = exercises.sumOf { it.targetReps } * 5
        val stars = if (currentExerciseIndex >= exercises.size) 3 else if (currentExerciseIndex >= 3) 2 else 1
        saveGameResult(maxScore = maxScore, stars = stars)
        
        // Send completion notification
        sendCompletionNotification(finalScore, totalReps, currentExerciseIndex)
        
        // Show completion dialog
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🎉 Workout Complete!")
            .setMessage("Total Reps: $totalReps\nExercises Completed: $currentExerciseIndex/${exercises.size}\n\nYou earned $finalScore points!")
            .setPositiveButton("Finish") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun sendCompletionNotification(finalScore: Int, reps: Int, exercisesCompleted: Int) {
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
            .setContentTitle("🎉 Workout Complete!")
            .setContentText("Fitness Challenge: $reps total reps - $finalScore points earned!")
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle()
                .bigText("Congratulations! You completed the Fitness Challenge\n\nTotal Reps: $reps\nExercises: $exercisesCompleted\nPoints: $finalScore\n\nKeep up the great work! 💪"))
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        
        notificationManager.notify(2002, notification)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        gameTimer?.cancel()
    }
}
