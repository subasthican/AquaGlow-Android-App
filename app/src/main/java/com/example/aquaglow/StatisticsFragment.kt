package com.example.aquaglow

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * StatisticsFragment displays mood trends and wellness statistics
 */
class StatisticsFragment : Fragment() {

    private lateinit var moodChart: LineChart
    private lateinit var averageMoodText: TextView
    private lateinit var totalMoodsText: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var gson: Gson

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupSharedPreferences()
        loadMoodData()
        setupChart()
    }

    private fun initializeViews(view: View) {
        moodChart = view.findViewById(R.id.moodChart)
        averageMoodText = view.findViewById(R.id.averageMoodText)
        totalMoodsText = view.findViewById(R.id.totalMoodsText)
    }

    private fun setupSharedPreferences() {
        sharedPreferences = requireContext().getSharedPreferences("aquaglow_prefs", 0)
        gson = Gson()
    }

    private fun loadMoodData() {
        val moodJson = sharedPreferences.getString("mood_entries", "[]")
        val type = object : TypeToken<List<MoodFragment.MoodEntry>>() {}.type
        val moodEntries = gson.fromJson<List<MoodFragment.MoodEntry>>(moodJson, type) ?: emptyList()
        
        // Process mood data for the last 7 days
        processMoodData(moodEntries)
    }

    private fun processMoodData(moodEntries: List<MoodFragment.MoodEntry>) {
        val calendar = Calendar.getInstance()
        val last7Days = mutableListOf<Pair<String, Float>>()
        
        // Get last 7 days
        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_MONTH, -i)
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            
            // Find mood entries for this date
            val dayMoods = moodEntries.filter { 
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.timestamp) == date 
            }
            
            val averageMood = if (dayMoods.isNotEmpty()) {
                dayMoods.map { it.moodScore }.average().toFloat()
            } else {
                0f // No data for this day
            }
            
            last7Days.add(Pair(date, averageMood))
        }
        
        setupChartData(last7Days)
        updateStatistics(moodEntries)
    }

    private fun setupChartData(dailyMoods: List<Pair<String, Float>>) {
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()
        
        dailyMoods.forEachIndexed { index, (date, mood) ->
            if (mood > 0) { // Only add entries with actual mood data
                entries.add(Entry(index.toFloat(), mood))
            }
            labels.add(SimpleDateFormat("MMM dd", Locale.getDefault()).format(
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)!!
            ))
        }

        val dataSet = LineDataSet(entries, "Mood Score").apply {
            color = Color.parseColor("#06B6D4") // Secondary color
            setCircleColor(Color.parseColor("#06B6D4"))
            lineWidth = 3f
            circleRadius = 6f
            setDrawFilled(true)
            fillColor = Color.parseColor("#2206B6D4") // Semi-transparent
            valueTextSize = 12f
            valueTextColor = Color.parseColor("#475569")
        }

        val lineData = LineData(dataSet)
        moodChart.data = lineData
    }

    private fun setupChart() {
        moodChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            
            // Configure X-axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                setDrawAxisLine(true)
                textColor = Color.parseColor("#64748B")
                textSize = 12f
            }
            
            // Configure Y-axis
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#E2E8F0")
                textColor = Color.parseColor("#64748B")
                textSize = 12f
                axisMinimum = 0f
                axisMaximum = 5f
                granularity = 1f
            }
            
            axisRight.isEnabled = false
            
            // Configure legend
            legend.isEnabled = false
            
            // Configure animation
            animateX(1000)
        }
    }

    private fun updateStatistics(moodEntries: List<MoodFragment.MoodEntry>) {
        if (moodEntries.isNotEmpty()) {
            val averageMood = moodEntries.map { it.moodScore }.average()
            val moodEmoji = when {
                averageMood >= 4.5 -> "🤩"
                averageMood >= 3.5 -> "😄"
                averageMood >= 2.5 -> "😊"
                averageMood >= 1.5 -> "😐"
                else -> "😢"
            }
            
            averageMoodText.text = "Average Mood: $moodEmoji ${String.format("%.1f", averageMood)}/5"
            totalMoodsText.text = "Total Moods Logged: ${moodEntries.size}"
        } else {
            averageMoodText.text = "No mood data yet"
            totalMoodsText.text = "Start logging your moods to see statistics!"
        }
    }
}