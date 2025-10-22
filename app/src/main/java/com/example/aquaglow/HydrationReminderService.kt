package com.example.aquaglow

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

/**
 * Service to handle hydration reminders with configurable intervals
 */
class HydrationReminderService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var reminderRunnable: Runnable? = null
    private var isReminderActive = false
    private var reminderInterval = 0L // in milliseconds

    companion object {
        const val ACTION_START_REMINDER = "start_reminder"
        const val ACTION_STOP_REMINDER = "stop_reminder"
        const val EXTRA_INTERVAL = "interval"
        const val EXTRA_INTERVAL_TYPE = "interval_type" // "seconds", "minutes", "hours"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "hydration_reminders"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start as foreground service immediately
        startForeground(NOTIFICATION_ID, createNotification("Hydration reminder service is starting..."))
        
        // Use handler to process the intent after a short delay to ensure service is properly started
        handler.post {
            when (intent?.action) {
                ACTION_START_REMINDER -> {
                    val interval = intent.getLongExtra(EXTRA_INTERVAL, 30)
                    val intervalType = intent.getStringExtra(EXTRA_INTERVAL_TYPE) ?: "minutes"
                    startReminder(interval, intervalType)
                }
                ACTION_STOP_REMINDER -> {
                    stopReminder()
                }
            }
        }
        return START_STICKY
    }

    private fun startReminder(interval: Long, intervalType: String) {
        // Convert interval to milliseconds
        reminderInterval = when (intervalType) {
            "seconds" -> interval * 1000
            "minutes" -> interval * 60 * 1000
            "hours" -> interval * 60 * 60 * 1000
            else -> interval * 60 * 1000 // default to minutes
        }

        if (reminderInterval <= 0) return

        stopReminder() // Stop any existing reminder

        isReminderActive = true
        // Update the notification to show reminder is active
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification("Hydration reminder is active"))

        reminderRunnable = object : Runnable {
            override fun run() {
                if (isReminderActive) {
                    showHydrationNotification()
                    handler.postDelayed(this, reminderInterval)
                }
            }
        }

        handler.post(reminderRunnable!!)
    }

    private fun stopReminder() {
        isReminderActive = false
        reminderRunnable?.let { handler.removeCallbacks(it) }
        reminderRunnable = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(Service.STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private fun showHydrationNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create intent to open the app
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("💧 Time to Hydrate!")
            .setContentText("Don't forget to drink some water to stay healthy!")
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVibrate(longArrayOf(0, 300, 100, 300))
            .build()

        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }

    private fun createNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AquaGlow Hydration")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopReminder()
    }
}
