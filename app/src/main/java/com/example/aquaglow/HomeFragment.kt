package com.example.aquaglow

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.aquaglow.AuthActivity.Companion.PREFS
import com.example.aquaglow.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        bindData()
    }

    private fun bindData() {
        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        val name = prefs.getString("user_email", null)?.substringBefore('@') ?: getString(R.string.guest)
        val habitProgress = prefs.getInt("habitProgressToday", 0)
        val lastMoodEmoji = prefs.getString("lastMoodEmoji", "🙂")
        val lastMoodTime = prefs.getString("lastMoodTime", null)
        val hydrationCount = prefs.getInt("hydrationCount", 0)
        val hydrationGoal = prefs.getInt("hydrationGoal", 8)

        val greeting = getGreeting()
        binding.textGreeting.text = getString(R.string.greeting_with_name, greeting, name)

        binding.textDate.text = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())

        binding.cardHabitsProgress.text = getString(R.string.habits_progress_format, habitProgress)
        val moodDetail = if (lastMoodTime != null) {
            getString(R.string.mood_last_logged_format, lastMoodEmoji, lastMoodTime)
        } else {
            getString(R.string.mood_not_logged)
        }
        binding.cardMoodSummary.text = moodDetail

        binding.cardHydrationSummary.text = getString(R.string.hydration_summary_format, hydrationCount, hydrationGoal)

        val quotes = resources.getStringArray(R.array.wellness_quotes)
        binding.textQuote.text = quotes.random()

        binding.buttonAddHabit.setOnClickListener {
            // TODO: Navigate to Habit Tracker Activity
        }
        binding.buttonLogMood.setOnClickListener {
            // TODO: Navigate to Mood Journal Activity
        }
        binding.buttonLogWater.setOnClickListener {
            val updated = hydrationCount + 1
            prefs.edit().putInt("hydrationCount", updated).apply()
            bindData()
        }
    }

    private fun getGreeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> getString(R.string.good_morning)
            in 12..17 -> getString(R.string.good_afternoon)
            else -> getString(R.string.good_evening)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


