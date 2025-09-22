package com.example.aquaglow

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * WorkManagerUtils handles scheduling background tasks for AquaGlow
 */
object WorkManagerUtils {

    private const val HYDRATION_WORK_NAME = "hydration_reminder"
    private const val HABIT_RESET_WORK_NAME = "habit_reset"

    /**
     * Schedules hydration reminders at the specified interval
     */
    fun scheduleHydrationReminder(context: Context, intervalMinutes: Int) {
        val workManager = WorkManager.getInstance(context)

        // Cancel existing hydration work
        workManager.cancelUniqueWork(HYDRATION_WORK_NAME)

        // Create constraints
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()

        // Create periodic work request
        val hydrationWork = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
            intervalMinutes.toLong(), TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        // Enqueue unique work
        workManager.enqueueUniquePeriodicWork(
            HYDRATION_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            hydrationWork
        )
    }

    /**
     * Schedules daily habit reset at midnight
     */
    fun scheduleHabitReset(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // Cancel existing habit reset work
        workManager.cancelUniqueWork(HABIT_RESET_WORK_NAME)

        // Create constraints
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        // Calculate delay until next midnight
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val delay = calendar.timeInMillis - now

        // Create one-time work request
        val habitResetWork = OneTimeWorkRequestBuilder<HabitResetWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        // Enqueue unique work
        workManager.enqueueUniqueWork(
            HABIT_RESET_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            habitResetWork
        )
    }
}