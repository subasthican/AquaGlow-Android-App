package com.example.aquaglow

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * HabitResetWorker resets daily habit progress at midnight
 */
class HabitResetWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        resetHabitProgress()
        return Result.success()
    }

    private fun resetHabitProgress() {
        val sharedPreferences = applicationContext.getSharedPreferences("aquaglow_prefs", 0)
        val gson = Gson()

        // Load habits list
        val habitsJson = sharedPreferences.getString("habits_list", "[]")
        val type = object : TypeToken<List<HabitsFragment.Habit>>() {}.type
        val habits = gson.fromJson<List<HabitsFragment.Habit>>(habitsJson, type) ?: emptyList()

        // Reset all habits to not completed
        val updatedHabits = habits.map { habit ->
            habit.copy(isCompleted = false)
        }

        // Save updated habits
        val updatedHabitsJson = gson.toJson(updatedHabits)
        sharedPreferences.edit()
            .putString("habits_list", updatedHabitsJson)
            .apply()

        // Clear today's progress
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val progressKey = "habits_progress_$today"
        sharedPreferences.edit().remove(progressKey).apply()
    }
}