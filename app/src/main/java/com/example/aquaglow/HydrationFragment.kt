package com.example.aquaglow

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.aquaglow.AuthActivity.Companion.PREFS
import com.example.aquaglow.databinding.FragmentHydrationBinding

class HydrationFragment : Fragment() {

    private var _binding: FragmentHydrationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHydrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindData()

        binding.sliderInterval.addOnChangeListener { _, _, _ ->
            binding.textInterval.text = getString(R.string.reminder_interval_hours, binding.sliderInterval.value.toInt())
        }

        binding.buttonStart.setOnClickListener { scheduleReminders() }
        binding.buttonStop.setOnClickListener { cancelReminders() }
        binding.buttonAddCup.setOnClickListener { addCup() }
    }

    override fun onResume() {
        super.onResume()
        bindData()
    }

    private fun bindData() {
        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val count = prefs.getInt("hydrationCount", 0)
        binding.textCount.text = getString(R.string.hydration_count_format, count)
    }

    private fun addCup() {
        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val updated = prefs.getInt("hydrationCount", 0) + 1
        prefs.edit().putInt("hydrationCount", updated).apply()
        bindData()
    }

    private fun scheduleReminders() {
        val context = requireContext()
        val intervalHours = binding.sliderInterval.value.toLong()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, HydrationReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(context, 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val triggerAt = System.currentTimeMillis() + intervalHours * 60 * 60 * 1000
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAt, intervalHours * 60 * 60 * 1000, pending)

        // persist schedule for Notifications page
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("hydrationReminderEnabled", true)
            .putInt("hydrationReminderIntervalHours", intervalHours.toInt())
            .putLong("hydrationReminderNextTime", triggerAt)
            .apply()
    }

    private fun cancelReminders() {
        val context = requireContext()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, HydrationReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(context, 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pending)

        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("hydrationReminderEnabled", false)
            .apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


