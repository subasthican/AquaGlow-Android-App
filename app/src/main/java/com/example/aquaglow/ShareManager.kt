package com.example.aquaglow

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.View
import java.text.SimpleDateFormat
import java.util.*

/**
 * ShareManager handles sharing achievements, progress, and wellness data
 */
object ShareManager {
    
    // Achievement sharing removed
    
    /**
     * Share daily wellness score
     */
    fun shareDailyScore(context: Context, score: Int) {
        val emoji = when {
            score >= 90 -> "🏆"
            score >= 70 -> "⭐"
            score >= 50 -> "👍"
            else -> "💪"
        }
        
        val shareText = "$emoji My daily wellness score is $score/100! I'm tracking my habits, mood, and wellness with AquaGlow. #AquaGlow #Wellness #DailyScore"
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Daily Score"))
    }
    
    /**
     * Share weekly progress
     */
    fun shareWeeklyProgress(context: Context, progress: Int) {
        val shareText = "📊 I completed $progress% of my weekly wellness goals! Every small step counts towards a healthier lifestyle. #AquaGlow #Wellness #Progress"
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Weekly Progress"))
    }
    
    /**
     * Share habit streak
     */
    fun shareHabitStreak(context: Context, streak: Int) {
        val shareText = "🔥 I've maintained my habit streak for $streak days! Consistency is key to building healthy habits. #AquaGlow #HabitStreak #Consistency"
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Habit Streak"))
    }
    
    /**
     * Share mood summary
     */
    fun shareMoodSummary(context: Context, moodEntries: List<MoodFragment.MoodEntry>) {
        val recentMoods = moodEntries.take(7) // Last 7 days
        val averageMood = if (recentMoods.isNotEmpty()) {
            recentMoods.map { it.moodScore }.average().toInt()
        } else 0
        
        val moodEmoji = when {
            averageMood >= 8 -> "😊"
            averageMood >= 6 -> "🙂"
            averageMood >= 4 -> "😐"
            else -> "😔"
        }
        
        val shareText = "$moodEmoji My mood has been ${getMoodDescription(averageMood)} this week! Tracking my emotions helps me understand myself better. #AquaGlow #MoodTracking #MentalHealth"
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Mood Summary"))
    }
    
    /**
     * Share step count
     */
    fun shareStepCount(context: Context, stepCount: Int, stepGoal: Int) {
        val percentage = if (stepGoal > 0) (stepCount.toFloat() / stepGoal * 100).toInt() else 0
        val emoji = when {
            percentage >= 100 -> "🏃‍♂️"
            percentage >= 75 -> "🚶‍♂️"
            else -> "👟"
        }
        
        val shareText = "$emoji I walked $stepCount steps today! ${if (percentage >= 100) "Goal achieved!" else "Working towards my $stepGoal step goal."} #AquaGlow #Fitness #Steps"
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Step Count"))
    }
    
    /**
     * Create a progress image for sharing
     */
    fun createProgressImage(context: Context, title: String, progress: Int, maxProgress: Int): Bitmap {
        val width = 400
        val height = 200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Background
        val backgroundPaint = Paint().apply {
            color = Color.parseColor("#1A1A2E")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        
        // Progress bar background
        val progressBgPaint = Paint().apply {
            color = Color.parseColor("#E0E0E0")
            style = Paint.Style.FILL
        }
        val progressRect = android.graphics.RectF(50f, 120f, width - 50f, 140f)
        canvas.drawRoundRect(progressRect, 10f, 10f, progressBgPaint)
        
        // Progress bar fill
        val progressFillPaint = Paint().apply {
            color = Color.parseColor("#00D4AA")
            style = Paint.Style.FILL
        }
        val progressWidth = (width - 100f) * (progress.toFloat() / maxProgress)
        val progressFillRect = android.graphics.RectF(50f, 120f, 50f + progressWidth, 140f)
        canvas.drawRoundRect(progressFillRect, 10f, 10f, progressFillPaint)
        
        // Title text
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        val titleX = (width - titlePaint.measureText(title)) / 2
        canvas.drawText(title, titleX, 50f, titlePaint)
        
        // Progress text
        val progressText = "$progress/$maxProgress"
        val progressPaint = Paint().apply {
            color = Color.WHITE
            textSize = 18f
            isAntiAlias = true
        }
        val progressX = (width - progressPaint.measureText(progressText)) / 2
        canvas.drawText(progressText, progressX, 100f, progressPaint)
        
        return bitmap
    }
    
    private fun getMoodDescription(averageMood: Int): String {
        return when {
            averageMood >= 8 -> "excellent"
            averageMood >= 6 -> "good"
            averageMood >= 4 -> "okay"
            else -> "challenging"
        }
    }
}





