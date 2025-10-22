package com.example.aquaglow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

/**
 * HealthMetricsFragment displays health metrics and tracking
 */
class HealthMetricsFragment : Fragment() {

    private lateinit var metricsRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var healthScoreText: TextView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var addMetricButton: MaterialButton
    private lateinit var refreshButton: MaterialButton

    private lateinit var metricsAdapter: HealthMetricsAdapter
    private var metricsList = mutableListOf<HealthMetricsManager.HealthMetric>()
    private var healthScore = 0
    private var loadingIndicator: ProgressBar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_health_metrics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackButton(view)

        initializeViews(view)
        setupRecyclerView()
        setupClickListeners()
        showLoading()
        loadMetrics()
        updateProgress()
        hideLoading()
        setupGlowEffects(view)
    }
    
    private fun showLoading() {
        loadingIndicator?.visibility = View.VISIBLE
        metricsRecyclerView.visibility = View.GONE
    }
    
    private fun hideLoading() {
        loadingIndicator?.visibility = View.GONE
        metricsRecyclerView.visibility = View.VISIBLE
    }

    private fun setupBackButton(view: View) {
        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initializeViews(view: View) {
        metricsRecyclerView = view.findViewById(R.id.metricsRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        progressText = view.findViewById(R.id.progressText)
        healthScoreText = view.findViewById(R.id.healthScoreText)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        addMetricButton = view.findViewById(R.id.addMetricButton)
        refreshButton = view.findViewById(R.id.refreshButton)
    }

    private fun setupRecyclerView() {
        metricsAdapter = HealthMetricsAdapter(metricsList) { metric ->
            viewMetric(metric)
        }
        metricsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = metricsAdapter
        }
    }

    private fun setupClickListeners() {
        addMetricButton.setOnClickListener {
            addNewMetric()
        }
        
        refreshButton.setOnClickListener {
            showLoading()
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                loadMetrics()
                hideLoading()
            }, 500)
        }
    }

    private fun loadMetrics() {
        val allMetrics = HealthMetricsManager.getAllMetrics(requireContext())
        metricsList.clear()
        metricsList.addAll(allMetrics)
        
        healthScore = HealthMetricsManager.calculateHealthScore(requireContext())
        
        metricsAdapter.notifyDataSetChanged()
        updateProgress()
        updateEmptyState()
    }

    private fun updateProgress() {
        val trackedMetrics = HealthMetricsManager.getTrackedMetricsCount(requireContext())
        val totalMetrics = metricsList.size
        val progress = if (totalMetrics > 0) (trackedMetrics * 100) / totalMetrics else 0
        
        progressBar.progress = progress
        progressText.text = "$trackedMetrics/$totalMetrics tracked"
        healthScoreText.text = "Health Score: $healthScore/100"
    }

    private fun updateEmptyState() {
        if (metricsList.isEmpty()) {
            emptyStateLayout.visibility = View.VISIBLE
            metricsRecyclerView.visibility = View.GONE
        } else {
            emptyStateLayout.visibility = View.GONE
            metricsRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun addNewMetric() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_health_metric, null)
        val metricNameInput = dialogView.findViewById<android.widget.EditText>(R.id.metricNameInput)
        val metricValueInput = dialogView.findViewById<android.widget.EditText>(R.id.metricValueInput)
        val metricUnitSpinner = dialogView.findViewById<android.widget.Spinner>(R.id.metricUnitSpinner)
        val metricCategorySpinner = dialogView.findViewById<android.widget.Spinner>(R.id.metricCategorySpinner)
        
        // Setup unit spinner
        val units = arrayOf("kg", "lbs", "cm", "inches", "bpm", "mmHg", "%", "hours", "minutes", "steps", "calories", "mg/dL", "custom")
        val unitAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, units)
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        metricUnitSpinner.adapter = unitAdapter
        
        // Setup category spinner
        val categories = arrayOf("Weight", "Blood Pressure", "Heart Rate", "Blood Sugar", "Sleep", "Activity", "Other")
        val categoryAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        metricCategorySpinner.adapter = categoryAdapter
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.health_metrics_add_title))
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = metricNameInput.text.toString().trim()
                val valueText = metricValueInput.text.toString().trim()
                val unit = metricUnitSpinner.selectedItem.toString()
                val category = metricCategorySpinner.selectedItem.toString()
                
                if (name.isNotEmpty() && valueText.isNotEmpty()) {
                    try {
                        val value = valueText.toFloat()
                        
                        // Map category to MetricType
                        val metricType = when (category) {
                            "Weight" -> HealthMetricsManager.MetricType.WEIGHT
                            "Blood Pressure" -> HealthMetricsManager.MetricType.BLOOD_PRESSURE
                            "Heart Rate" -> HealthMetricsManager.MetricType.HEART_RATE
                            "Blood Sugar" -> HealthMetricsManager.MetricType.BLOOD_SUGAR
                            "Sleep" -> HealthMetricsManager.MetricType.SLEEP_HOURS
                            "Activity" -> HealthMetricsManager.MetricType.CALORIES
                            else -> HealthMetricsManager.MetricType.STRESS_LEVEL
                        }
                        
                        val metric = HealthMetricsManager.HealthMetric(
                            id = java.util.UUID.randomUUID().toString(),
                            name = name,
                            type = metricType,
                            unit = unit,
                            value = value,
                            timestamp = System.currentTimeMillis(),
                            notes = ""
                        )
                        HealthMetricsManager.addHealthMetric(requireContext(), metric)
                        loadMetrics()
                        android.widget.Toast.makeText(requireContext(), "Metric added successfully!", android.widget.Toast.LENGTH_SHORT).show()
                    } catch (e: NumberFormatException) {
                        android.widget.Toast.makeText(requireContext(), "Please enter a valid number for value", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } else {
                    android.widget.Toast.makeText(requireContext(), "Please fill all fields", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun viewMetric(metric: HealthMetricsManager.HealthMetric) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_view_health_metric, null)
        
        dialogView.findViewById<TextView>(R.id.metricNameText).text = metric.name
        dialogView.findViewById<TextView>(R.id.metricValueText).text = "${metric.value} ${metric.unit}"
        dialogView.findViewById<TextView>(R.id.metricCategoryText).text = "Type: ${metric.type.name.replace("_", " ")}"
        dialogView.findViewById<TextView>(R.id.metricDateText).text = "Recorded: ${java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(metric.timestamp))}"
        dialogView.findViewById<TextView>(R.id.metricNotesText).apply {
            text = if (metric.notes.isNotEmpty()) metric.notes else "No notes"
            visibility = View.VISIBLE
        }
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Health Metric Details")
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .setNeutralButton("Delete") { _, _ ->
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete Metric?")
                    .setMessage("Are you sure you want to delete this metric: ${metric.name}?")
                    .setPositiveButton("Delete") { _, _ ->
                        HealthMetricsManager.deleteHealthMetric(requireContext(), metric.id)
                        loadMetrics()
                        android.widget.Toast.makeText(requireContext(), "Metric deleted", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            .show()
    }

    private fun setupGlowEffects(view: View) {
        // Apply glow effects to buttons
        addMetricButton.let { button ->
            button.setOnTouchListener { _, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        button.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                        true
                    }
                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                        button.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                        false
                    }
                    else -> false
                }
            }
        }
        
        refreshButton.let { button ->
            button.setOnTouchListener { _, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        button.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                        true
                    }
                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                        button.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                        false
                    }
                    else -> false
                }
            }
        }
    }
}

/**
 * Adapter for health metrics RecyclerView
 */
class HealthMetricsAdapter(
    private val metrics: List<HealthMetricsManager.HealthMetric>,
    private val onMetricClick: (HealthMetricsManager.HealthMetric) -> Unit
) : RecyclerView.Adapter<HealthMetricsAdapter.MetricViewHolder>() {

    class MetricViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.metricNameText)
        val valueText: TextView = itemView.findViewById(R.id.metricValueText)
        val unitText: TextView = itemView.findViewById(R.id.metricUnitText)
        val categoryText: TextView = itemView.findViewById(R.id.metricCategoryText)
        val statusText: TextView = itemView.findViewById(R.id.metricStatusText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MetricViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_health_metric, parent, false)
        return MetricViewHolder(view)
    }

    override fun onBindViewHolder(holder: MetricViewHolder, position: Int) {
        val metric = metrics[position]
        
        holder.nameText.text = metric.name
        holder.valueText.text = metric.value.toString()
        holder.unitText.text = metric.unit
        holder.categoryText.text = metric.type.name.replace("_", " ")
        
        // Determine status based on metric type
        holder.statusText.text = getMetricStatus(metric)
        
        holder.itemView.setOnClickListener {
            onMetricClick(metric)
        }
    }
    
    private fun getMetricStatus(metric: HealthMetricsManager.HealthMetric): String {
        return when (metric.type) {
            // Lower is better
            HealthMetricsManager.MetricType.STRESS_LEVEL,
            HealthMetricsManager.MetricType.PAIN_LEVEL -> {
                when {
                    metric.value <= 3 -> "✓ Low"
                    metric.value <= 6 -> "⚠ Moderate"
                    else -> "✗ High"
                }
            }
            
            // Blood pressure (systolic)
            HealthMetricsManager.MetricType.BLOOD_PRESSURE -> {
                when {
                    metric.value < 90 -> "⚠ Low"
                    metric.value <= 120 -> "✓ Normal"
                    metric.value <= 140 -> "⚠ Elevated"
                    else -> "✗ High"
                }
            }
            
            // Blood sugar
            HealthMetricsManager.MetricType.BLOOD_SUGAR -> {
                when {
                    metric.value < 70 -> "⚠ Low"
                    metric.value <= 100 -> "✓ Normal"
                    metric.value <= 125 -> "⚠ Pre-diabetic"
                    else -> "✗ High"
                }
            }
            
            // Heart rate
            HealthMetricsManager.MetricType.HEART_RATE -> {
                when {
                    metric.value < 60 -> "⚠ Low"
                    metric.value <= 100 -> "✓ Normal"
                    else -> "⚠ High"
                }
            }
            
            // Sleep hours
            HealthMetricsManager.MetricType.SLEEP_HOURS -> {
                when {
                    metric.value < 6 -> "✗ Too Low"
                    metric.value <= 9 -> "✓ Good"
                    else -> "⚠ Too High"
                }
            }
            
            // Water intake (liters)
            HealthMetricsManager.MetricType.WATER_INTAKE -> {
                when {
                    metric.value < 1.5 -> "✗ Low"
                    metric.value <= 3 -> "✓ Good"
                    else -> "⚠ Too High"
                }
            }
            
            // Energy level (out of 10)
            HealthMetricsManager.MetricType.ENERGY_LEVEL -> {
                when {
                    metric.value <= 3 -> "✗ Low"
                    metric.value <= 7 -> "⚠ Moderate"
                    else -> "✓ High"
                }
            }
            
            // For other metrics with target values
            else -> {
                metric.target?.let { target ->
                    val diff = kotlin.math.abs(metric.value - target)
                    val tolerance = target * 0.1f // 10% tolerance
                    when {
                        diff <= tolerance -> "✓ On Target"
                        diff <= tolerance * 2 -> "⚠ Near Target"
                        else -> "✗ Off Target"
                    }
                } ?: "⚬ Recorded"
            }
        }
    }

    override fun getItemCount(): Int = metrics.size
}
