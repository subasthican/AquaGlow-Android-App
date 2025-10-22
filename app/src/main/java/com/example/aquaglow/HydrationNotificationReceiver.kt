package com.example.aquaglow

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

/**
 * HydrationNotificationReceiver handles "I Drank Water" action from notification
 */
class HydrationNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == HydrationReminderWorker.ACTION_ADD_WATER) {
            // Add 1 glass of water
            addWater(context, 1)
            
            // Dismiss the notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(1001)
            
            // Show confirmation toast
            Toast.makeText(context, "✓ Added 1 glass of water! 💧", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addWater(context: Context, glasses: Int) {
        val sharedPreferences = context.getSharedPreferences("aquaglow_prefs", Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        // Get current water intake for today
        val currentGlasses = sharedPreferences.getInt("water_intake_$today", 0)
        val newTotal = currentGlasses + glasses
        
        // Save updated total
        sharedPreferences.edit()
            .putInt("water_intake_$today", newTotal)
            .apply()
    }
}




