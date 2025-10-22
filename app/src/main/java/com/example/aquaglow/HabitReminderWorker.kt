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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * HabitReminderWorker sends notifications to remind users about their habits
 */
class HabitReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        private const val CHANNEL_ID = "habit_reminder_channel"
        private const val NOTIFICATION_ID = 2001
        const val ACTION_MARK_COMPLETE = "com.example.aquaglow.MARK_HABIT_COMPLETE"
    }

    override fun doWork(): Result {
        createNotificationChannel()
        sendHabitReminder()
        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to complete your daily habits"
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendHabitReminder() {
        val habitId = inputData.getString("habit_id")
        val habitName = inputData.getString("habit_name") ?: "Habit"
        val customMessage = inputData.getString("custom_message")
        
        if (habitId == null) {
            return
        }

        val openAppIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_habits", true)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent to mark habit as complete directly from notification
        val markCompleteIntent = Intent(applicationContext, HabitNotificationReceiver::class.java).apply {
            action = ACTION_MARK_COMPLETE
            putExtra("habit_id", habitId)
        }
        val markCompletePendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            habitId.hashCode(),
            markCompleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "🎯 Habit Reminder"
        val message = customMessage?.takeIf { it.isNotEmpty() } 
            ?: "Time to complete: $habitName"

        // Build notification with action buttons
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$message\n\nTap 'Mark Complete' to check it off or open the app to manage your habits."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                R.drawable.ic_notifications,
                "Mark Complete",
                markCompletePendingIntent
            )
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .build()

        val notificationManager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID + habitId.hashCode() % 1000, notification)
    }
}


