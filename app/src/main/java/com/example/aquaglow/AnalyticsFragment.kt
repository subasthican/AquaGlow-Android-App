package com.example.aquaglow

import android.os.Bundle
import java.util.Locale
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

/**
 * AnalyticsFragment displays advanced analytics and insights
 */
class AnalyticsFragment : Fragment() {

    private lateinit var moodTrendChart: LineChart
    private lateinit var habitCompletionChart: BarChart
    private lateinit var habitCategoryChart: PieChart
    private lateinit var insightsRecyclerView: RecyclerView
    private lateinit var trendScoreText: TextView
    private lateinit var trendDirectionText: TextView
    private lateinit var trendDescriptionText: TextView
    private lateinit var improvementText: TextView
    private lateinit var weekButton: com.google.android.material.button.MaterialButton
    private lateinit var monthButton: com.google.android.material.button.MaterialButton
    private lateinit var quarterButton: com.google.android.material.button.MaterialButton
    private lateinit var exportButton: com.google.android.material.button.MaterialButton

    private lateinit var insightsAdapter: InsightsAdapter
    private var currentPeriod = 30 // days
    private var insightsList = mutableListOf<AnalyticsManager.Insight>()
    private var loadingOverlay: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_analytics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackButton(view)

        initializeViews(view)
        setupCharts()
        setupRecyclerView()
        setupClickListeners()
        showLoading()
        loadData()
        hideLoading()
        setupGlowEffects(view)
    }
    
    private fun showLoading() {
        loadingOverlay?.visibility = View.VISIBLE
    }
    
    private fun hideLoading() {
        loadingOverlay?.visibility = View.GONE
    }

    private fun setupBackButton(view: View) {
        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initializeViews(view: View) {
        moodTrendChart = view.findViewById(R.id.moodTrendChart)
        habitCompletionChart = view.findViewById(R.id.habitCompletionChart)
        habitCategoryChart = view.findViewById(R.id.habitCategoryChart)
        insightsRecyclerView = view.findViewById(R.id.insightsRecyclerView)
        trendScoreText = view.findViewById(R.id.trendScoreText)
        trendDirectionText = view.findViewById(R.id.trendDirectionText)
        trendDescriptionText = view.findViewById(R.id.trendDescriptionText)
        improvementText = view.findViewById(R.id.improvementText)
        weekButton = view.findViewById(R.id.weekButton)
        monthButton = view.findViewById(R.id.monthButton)
        quarterButton = view.findViewById(R.id.quarterButton)
        exportButton = view.findViewById(R.id.exportButton)
        loadingOverlay = view.findViewById(R.id.loadingOverlay)
    }

    private fun setupCharts() {
        setupMoodTrendChart()
        setupHabitCompletionChart()
        setupHabitCategoryChart()
    }

    private fun setupMoodTrendChart() {
        moodTrendChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setDragEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(true)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
                axisMaximum = 10f
            }
            
            axisRight.isEnabled = false
            legend.isEnabled = false
        }
    }

    private fun setupHabitCompletionChart() {
        habitCompletionChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setDragEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(true)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
                axisMaximum = 100f
            }
            
            axisRight.isEnabled = false
            legend.isEnabled = false
        }
    }

    private fun setupHabitCategoryChart() {
        habitCategoryChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setHoleColor(android.graphics.Color.TRANSPARENT)
            setTransparentCircleColor(android.graphics.Color.TRANSPARENT)
            setTransparentCircleAlpha(110)
            setHoleRadius(58f)
            setTransparentCircleRadius(61f)
            setDrawCenterText(true)
            setDrawEntryLabels(true)
            setUsePercentValues(true)
            setEntryLabelTextSize(12f)
            setEntryLabelColor(android.graphics.Color.WHITE)
            legend.isEnabled = false
        }
    }

    private fun setupRecyclerView() {
        insightsAdapter = InsightsAdapter(insightsList)
        insightsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = insightsAdapter
        }
    }

    private fun setupClickListeners() {
        weekButton.setOnClickListener {
            currentPeriod = 7
            updatePeriodButtons()
            showLoading()
            loadData()
            hideLoading()
        }
        
        monthButton.setOnClickListener {
            currentPeriod = 30
            updatePeriodButtons()
            showLoading()
            loadData()
            hideLoading()
        }
        
        quarterButton.setOnClickListener {
            currentPeriod = 90
            updatePeriodButtons()
            showLoading()
            loadData()
            hideLoading()
        }
        
        exportButton.setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Exporting data...", android.widget.Toast.LENGTH_SHORT).show()
            // Export would happen here in real implementation
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                android.widget.Toast.makeText(requireContext(), "Export complete! (Feature in development)", android.widget.Toast.LENGTH_SHORT).show()
            }, 1500)
        }
    }

    private fun updatePeriodButtons() {
        val selectedColor = android.graphics.Color.parseColor("#00D4AA")
        val unselectedColor = android.graphics.Color.parseColor("#E0E0E0")
        
        weekButton.setBackgroundColor(if (currentPeriod == 7) selectedColor else unselectedColor)
        monthButton.setBackgroundColor(if (currentPeriod == 30) selectedColor else unselectedColor)
        quarterButton.setBackgroundColor(if (currentPeriod == 90) selectedColor else unselectedColor)
    }

    private fun loadData() {
        loadTrendData()
        loadCharts()
        loadInsights()
    }

    private fun loadTrendData() {
        val trendData = AnalyticsManager.getTrendAnalysis(requireContext(), currentPeriod)
        
        trendScoreText.text = String.format(Locale.getDefault(), "%.0f", trendData.averageScore)
        
        when (trendData.trend) {
            AnalyticsManager.TrendDirection.UP -> {
                trendDirectionText.text = "📈"
                trendDescriptionText.text = "Improving"
                improvementText.text = "+${String.format(Locale.getDefault(), "%.0f", trendData.improvement)}%"
                improvementText.setTextColor(android.graphics.Color.parseColor("#00D4AA"))
            }
            AnalyticsManager.TrendDirection.DOWN -> {
                trendDirectionText.text = "📉"
                trendDescriptionText.text = "Declining"
                improvementText.text = "${String.format(Locale.getDefault(), "%.0f", trendData.improvement)}%"
                improvementText.setTextColor(android.graphics.Color.parseColor("#FF6B6B"))
            }
            AnalyticsManager.TrendDirection.STABLE -> {
                trendDirectionText.text = "➡️"
                trendDescriptionText.text = "Stable"
                improvementText.text = "0%"
                improvementText.setTextColor(android.graphics.Color.parseColor("#FFA500"))
            }
        }
    }

    private fun loadCharts() {
        // Load mood trend chart
        val moodData = AnalyticsManager.getMoodTrendData(requireContext(), currentPeriod)
        moodTrendChart.data = moodData
        moodTrendChart.invalidate()
        
        // Load habit completion chart
        val habitData = AnalyticsManager.getHabitCompletionData(requireContext(), currentPeriod)
        habitCompletionChart.data = habitData
        habitCompletionChart.invalidate()
        
        // Load habit category pie chart
        val categoryData = AnalyticsManager.getHabitCategoryData(requireContext())
        habitCategoryChart.data = categoryData
        habitCategoryChart.invalidate()
    }

    private fun loadInsights() {
        insightsList.clear()
        insightsList.addAll(AnalyticsManager.generateInsights(requireContext()))
        insightsAdapter.notifyDataSetChanged()
    }

    private fun setupGlowEffects(view: View) {
        // Add breathing effect to trend cards
        GlowAnimationUtils.createBreathingEffect(trendScoreText, 3000L)
        GlowAnimationUtils.createBreathingEffect(improvementText, 3000L)
        
        // Add pulse effect to export button
        GlowAnimationUtils.createPulseEffect(exportButton, 2000L)
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }
}

/**
 * Adapter for displaying insights
 */
class InsightsAdapter(
    private val insights: List<AnalyticsManager.Insight>
) : RecyclerView.Adapter<InsightsAdapter.InsightViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsightViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_insight, parent, false)
        return InsightViewHolder(view)
    }

    override fun onBindViewHolder(holder: InsightViewHolder, position: Int) {
        holder.bind(insights[position])
    }

    override fun getItemCount(): Int = insights.size

    class InsightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: TextView = itemView.findViewById(R.id.insightIcon)
        private val title: TextView = itemView.findViewById(R.id.insightTitle)
        private val description: TextView = itemView.findViewById(R.id.insightDescription)
        private val recommendation: TextView = itemView.findViewById(R.id.insightRecommendation)
        private val confidence: TextView = itemView.findViewById(R.id.insightConfidence)

        fun bind(insight: AnalyticsManager.Insight) {
            icon.text = when (insight.type) {
                AnalyticsManager.InsightType.HABIT -> "🎯"
                AnalyticsManager.InsightType.MOOD -> "😊"
                AnalyticsManager.InsightType.FITNESS -> "🏃‍♂️"
                AnalyticsManager.InsightType.SLEEP -> "😴"
                AnalyticsManager.InsightType.STRESS -> "🧘‍♀️"
                AnalyticsManager.InsightType.GENERAL -> "💡"
            }
            
            title.text = insight.title
            description.text = insight.description
            recommendation.text = "💡 ${insight.recommendation}"
            confidence.text = "${(insight.confidence * 100).toInt()}% confidence"
        }
    }
    
}
