package com.example.aquaglow

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.example.aquaglow.AuthActivity.Companion.PREFS

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey)

        val themePref = findPreference<ListPreference>("themeMode")
        val remindersPref = findPreference<SwitchPreferenceCompat>("hydrationRemindersEnabled")
        val clearDataPref = findPreference<Preference>("clearData")

        themePref?.setOnPreferenceChangeListener { _, newValue ->
            when (newValue as String) {
                "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            true
        }

        remindersPref?.setOnPreferenceChangeListener { _, newVal ->
            val enable = newVal as Boolean
            val ctx = requireContext()
            val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            prefs.edit().putBoolean("hydrationReminderEnabled", enable).apply()
            if (enable) scheduleFromPrefs(ctx) else cancelReminders(ctx)
            true
        }

        clearDataPref?.setOnPreferenceClickListener {
            val ctx = requireContext()
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
            PreferenceManager.getDefaultSharedPreferences(ctx).edit().clear().apply()
            cancelReminders(ctx)
            // Back to first launch: Splash -> Onboarding
            startActivity(Intent(ctx, SplashActivity::class.java))
            requireActivity().finish()
            true
        }
    }

    private fun scheduleFromPrefs(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val interval = prefs.getInt("hydrationReminderIntervalHours", 2)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, HydrationReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(context, 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val triggerAt = System.currentTimeMillis() + interval * 60L * 60L * 1000L
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAt, interval * 60L * 60L * 1000L, pending)
        prefs.edit().putLong("hydrationReminderNextTime", triggerAt).apply()
    }

    private fun cancelReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, HydrationReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(context, 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pending)
    }
}


