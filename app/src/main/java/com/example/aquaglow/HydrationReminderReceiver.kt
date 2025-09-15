package com.example.aquaglow

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.aquaglow.AuthActivity.Companion.PREFS
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HydrationReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val channelId = "hydration_channel"
        ensureChannel(context, channelId)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.time_to_drink_water))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(context).notify(2001, notification)

        // log to history (keep last 20)
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val gson = Gson()
        val type = object : TypeToken<MutableList<Long>>() {}.type
        val json = prefs.getString("notificationHistory", null)
        val list: MutableList<Long> = if (!json.isNullOrEmpty()) gson.fromJson(json, type) else mutableListOf()
        list.add(0, System.currentTimeMillis())
        if (list.size > 20) list.subList(20, list.size).clear()
        prefs.edit().putString("notificationHistory", gson.toJson(list)).apply()
    }

    private fun ensureChannel(context: Context, channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = context.getSystemService(android.app.NotificationManager::class.java)
            val existing = mgr.getNotificationChannel(channelId)
            if (existing == null) {
                val channel = android.app.NotificationChannel(channelId, "Hydration", android.app.NotificationManager.IMPORTANCE_HIGH)
                channel.description = "Hydration reminders"
                mgr.createNotificationChannel(channel)
            }
        }
    }
}


