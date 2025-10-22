package com.example.aquaglow

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

/**
 * StepsFragment - Track daily steps and shake count
 */
class StepsFragment : Fragment(), SensorEventListener {

    private lateinit var stepsCountText: android.widget.TextView
    private lateinit var stepsProgressBar: android.widget.ProgressBar
    private lateinit var stepsPercentageText: android.widget.TextView
    private lateinit var shakeCountText: android.widget.TextView
    private lateinit var dailyGoalText: android.widget.TextView
    private lateinit var progressText: android.widget.TextView
    private lateinit var resetButton: com.google.android.material.button.MaterialButton
    private lateinit var goalButton: com.google.android.material.button.MaterialButton

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null

    private var currentSteps = 0
    private var currentShakeCount = 0
    private var dailyGoal = 10000
    private var lastShakeTime = 0L
    private val shakeThreshold = 15f
    private val shakeDelay = 1000L // 1 second between shakes

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_steps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupSharedPreferences()
        setupSensors()
        loadStepsData()
        setupClickListeners()
        updateUI()
    }
    
    private fun initializeViews(view: View) {
        stepsCountText = view.findViewById(R.id.stepsCountText)
        stepsProgressBar = view.findViewById(R.id.stepsProgressBar)
        stepsPercentageText = view.findViewById(R.id.stepsPercentageText)
        shakeCountText = view.findViewById(R.id.shakeCountText)
        dailyGoalText = view.findViewById(R.id.dailyGoalText)
        progressText = view.findViewById(R.id.progressText)
        resetButton = view.findViewById(R.id.resetButton)
        goalButton = view.findViewById(R.id.goalButton)
    }

    private fun setupSharedPreferences() {
        sharedPreferences = requireContext().getSharedPreferences("aquaglow_prefs", Context.MODE_PRIVATE)
        dailyGoal = sharedPreferences.getInt("steps_daily_goal", 10000)
    }

    private fun setupSensors() {
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        
        // Step counter sensor
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        
        // Accelerometer for shake detection
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private fun loadStepsData() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        currentSteps = sharedPreferences.getInt("steps_$today", 0)
        currentShakeCount = sharedPreferences.getInt("shake_count_$today", 0)
    }

    private fun saveStepsData() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        sharedPreferences.edit()
            .putInt("steps_$today", currentSteps)
            .putInt("shake_count_$today", currentShakeCount)
            .apply()
        
        // Refresh widget when step data changes
        refreshWidget()
    }
    
    private fun refreshWidget() {
        try {
            val intent = Intent(requireContext(), AquaGlowWidgetProvider::class.java).apply {
                action = AquaGlowWidgetProvider.ACTION_WIDGET_UPDATE
            }
            requireContext().sendBroadcast(intent)
        } catch (e: Exception) {
            android.util.Log.e("StepsFragment", "Error refreshing widget", e)
        }
    }

    private fun setupClickListeners() {
        resetButton.setOnClickListener {
            showResetConfirmation()
        }
        
        goalButton.setOnClickListener {
            showGoalDialog()
        }
    }

    private fun updateUI() {
        // Update steps
        stepsCountText.text = currentSteps.toString()
        stepsProgressBar.max = dailyGoal
        stepsProgressBar.progress = currentSteps
        
        val stepsPercentage = if (dailyGoal > 0) (currentSteps * 100 / dailyGoal) else 0
        stepsPercentageText.text = "$stepsPercentage%"
        
        // Update shake count
        shakeCountText.text = currentShakeCount.toString()
        
        // Update goal
        dailyGoalText.text = "Goal: $dailyGoal steps"
        
        // Update progress text
        val remainingSteps = dailyGoal - currentSteps
        if (remainingSteps > 0) {
            progressText.text = "$remainingSteps steps remaining"
        } else {
            progressText.text = "🎉 Goal achieved! Great job!"
        }
    }

    private fun showResetConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Reset Steps")
            .setMessage("Are you sure you want to reset today's steps and shake count?")
            .setPositiveButton("Reset") { _, _ ->
                resetSteps()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun resetSteps() {
        currentSteps = 0
        currentShakeCount = 0
        saveStepsData()
        updateUI()
        
        android.widget.Toast.makeText(requireContext(), "Steps and shake count reset!", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun showGoalDialog() {
        val input = android.widget.EditText(requireContext()).apply {
            setText(dailyGoal.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Set Daily Goal")
            .setMessage("Enter your daily step goal:")
            .setView(input)
            .setPositiveButton("Set") { _, _ ->
                val newGoal = input.text.toString().toIntOrNull() ?: dailyGoal
                if (newGoal > 0) {
                    dailyGoal = newGoal
                    sharedPreferences.edit().putInt("steps_daily_goal", dailyGoal).apply()
                    updateUI()
                    android.widget.Toast.makeText(requireContext(), "Goal updated to $dailyGoal steps!", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadStepsData()
        updateUI()
        
        // Register sensors
        stepSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        accelerometerSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            when (sensorEvent.sensor.type) {
                Sensor.TYPE_STEP_COUNTER -> {
                    // Step counter gives total steps since last boot
                    // We need to track daily steps
                    val totalSteps = sensorEvent.values[0].toInt()
                    val lastTotalSteps = sharedPreferences.getInt("last_total_steps", 0)
                    
                    if (lastTotalSteps == 0) {
                        // First time, just store the current total
                        sharedPreferences.edit().putInt("last_total_steps", totalSteps).apply()
                    } else {
                        // Calculate daily steps
                        val dailySteps = totalSteps - lastTotalSteps
                        if (dailySteps > 0) {
                            currentSteps += dailySteps
                            sharedPreferences.edit()
                                .putInt("last_total_steps", totalSteps)
                                .apply()
                            saveStepsData()
                            updateUI()
                        }
                    }
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    // Shake detection
                    val x = sensorEvent.values[0]
                    val y = sensorEvent.values[1]
                    val z = sensorEvent.values[2]
                    
                    val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                    
                    if (acceleration > shakeThreshold) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastShakeTime > shakeDelay) {
                            currentShakeCount++
                            lastShakeTime = currentTime
                            saveStepsData()
                            updateUI()
                            
                            // Show shake feedback
                            android.widget.Toast.makeText(requireContext(), "Shake detected! Count: $currentShakeCount", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sensorManager.unregisterListener(this)
    }
}
