package com.example.aquaglow

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * BroadcastReceiver to handle habit notification actions
 */
class HabitNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            HabitReminderWorker.ACTION_MARK_COMPLETE -> {
                val habitId = intent.getStringExtra("habit_id")
                if (habitId != null) {
                    markHabitComplete(context, habitId)
                }
            }
        }
    }

    private fun markHabitComplete(context: Context, habitId: String) {
        try {
            val sharedPreferences = context.getSharedPreferences("aquaglow_prefs", 0)
            val gson = Gson()
            
            // Load habits
            val habitsJson = sharedPreferences.getString("habits_list", "[]")
            val type = object : TypeToken<List<HabitsFragment.Habit>>() {}.type
            val habits = gson.fromJson<List<HabitsFragment.Habit>>(habitsJson, type) ?: emptyList()
            
            // Find the habit
            val habit = habits.find { it.id == habitId }
            if (habit != null) {
                // Mark as completed for today
                val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                val progressKey = "habits_progress_$today"
                val progressJson = sharedPreferences.getString(progressKey, "{}")
                val progressType = object : TypeToken<Map<String, Boolean>>() {}.type
                val progress = gson.fromJson<Map<String, Boolean>>(progressJson, progressType)?.toMutableMap() ?: mutableMapOf()
                
                progress[habitId] = true
                val updatedProgressJson = gson.toJson(progress)
                sharedPreferences.edit().putString(progressKey, updatedProgressJson).apply()
                
                // Show confirmation
                Toast.makeText(context, "✅ ${habit.name} marked as complete!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Habit not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error marking habit complete", Toast.LENGTH_SHORT).show()
        }
    }
}


