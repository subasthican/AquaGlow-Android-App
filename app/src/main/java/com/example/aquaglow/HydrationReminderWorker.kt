package com.example.aquaglow

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * HydrationReminderWorker sends notifications to remind users to drink water
 */
class HydrationReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        private const val CHANNEL_ID = "hydration_reminder_channel"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_ADD_WATER = "com.example.aquaglow.ADD_WATER"
    }

    override fun doWork(): Result {
        createNotificationChannel()
        sendHydrationNotification()
        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_HIGH  // Changed to HIGH for better visibility
            ).apply {
                description = "Reminders to drink water and stay hydrated"
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendHydrationNotification() {
        // Intent to open app on Track tab (Hydration)
        val openAppIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_hydration", true)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent to add water directly from notification
        val addWaterIntent = Intent(applicationContext, HydrationNotificationReceiver::class.java).apply {
            action = ACTION_ADD_WATER
        }
        val addWaterPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            1,
            addWaterIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification with action buttons
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("💧 Time to Hydrate!")
            .setContentText("Tap to track your water intake - Stay healthy and energized!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Remember to drink water! Tap 'I Drank Water' to log it or open the app to track your hydration."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                R.drawable.ic_water_drop,
                "I Drank Water",
                addWaterPendingIntent
            )
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .build()

        val notificationManager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}


