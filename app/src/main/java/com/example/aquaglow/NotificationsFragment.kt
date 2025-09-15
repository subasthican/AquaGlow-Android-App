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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aquaglow.AuthActivity.Companion.PREFS
import com.example.aquaglow.databinding.FragmentNotificationsBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val gson = Gson()

    private val upcoming: MutableList<ReminderSchedule> = mutableListOf()
    private val history: MutableList<Long> = mutableListOf()
    private lateinit var upcomingAdapter: UpcomingAdapter
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.title = getString(R.string.notifications)
        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        upcomingAdapter = UpcomingAdapter(upcoming, object : UpcomingAdapter.Listener {
            override fun onToggleEnabled(enabled: Boolean) {
                val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                prefs.edit().putBoolean("hydrationReminderEnabled", enabled).apply()
                if (enabled) reschedule() else cancel()
                loadData(); bindData()
            }
        })
        binding.recyclerUpcoming.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerUpcoming.adapter = upcomingAdapter

        historyAdapter = HistoryAdapter(history)
        binding.recyclerHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerHistory.adapter = historyAdapter

        binding.buttonTest.setOnClickListener { sendTest() }
        binding.buttonClearHistory.setOnClickListener { clearHistory() }

        loadData(); bindData()
    }

    private fun loadData() {
        upcoming.clear()
        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean("hydrationReminderEnabled", false)
        val interval = prefs.getInt("hydrationReminderIntervalHours", 2)
        val nextAt = prefs.getLong("hydrationReminderNextTime", 0L)
        upcoming.add(ReminderSchedule(enabled, interval, nextAt))

        history.clear()
        val json = prefs.getString("notificationHistory", null)
        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<Long>>() {}.type
            history.addAll(gson.fromJson(json, type))
        }
    }

    private fun bindData() {
        upcomingAdapter.notifyDataSetChanged()
        historyAdapter.notifyDataSetChanged()
        binding.emptyHistory.visibility = if (history.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun reschedule() {
        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val interval = prefs.getInt("hydrationReminderIntervalHours", 2)
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), HydrationReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(requireContext(), 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val triggerAt = System.currentTimeMillis() + interval * 60L * 60L * 1000L
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAt, interval * 60L * 60L * 1000L, pending)
        prefs.edit().putLong("hydrationReminderNextTime", triggerAt).apply()
    }

    private fun cancel() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), HydrationReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(requireContext(), 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pending)
    }

    private fun sendTest() {
        HydrationReminderReceiver().onReceive(requireContext(), Intent(requireContext(), HydrationReminderReceiver::class.java))
        loadData(); bindData()
    }

    private fun clearHistory() {
        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().remove("notificationHistory").apply()
        loadData(); bindData()
    }
}

data class ReminderSchedule(var enabled: Boolean, var intervalHours: Int, var nextTimeMillis: Long)


