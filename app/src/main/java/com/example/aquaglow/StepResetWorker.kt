package com.example.aquaglow

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * StepResetWorker resets daily step count at midnight
 */
class StepResetWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        SensorManager.resetDailyStepCount(applicationContext)
        return Result.success()
    }
}







