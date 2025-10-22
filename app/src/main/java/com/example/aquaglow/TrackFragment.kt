package com.example.aquaglow

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * TrackFragment - Main tracking hub with tabs for Habits, Mood, and Hydration
 */
class TrackFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_track, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewPager)

        setupViewPager()
    }

    private fun setupViewPager() {
        val adapter = TrackPagerAdapter(this)
        viewPager.adapter = adapter
        
        // Keep all fragments in memory to prevent data loss
        viewPager.offscreenPageLimit = 2

        // Connect TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Habits"
                    tab.setIcon(R.drawable.ic_habits_nav)
                }
                1 -> {
                    tab.text = "Mood"
                    tab.setIcon(R.drawable.ic_mood_nav)
                }
                2 -> {
                    tab.text = "Hydration"
                    tab.setIcon(R.drawable.ic_water_drop)
                }
            }
        }.attach()
    }

    private inner class TrackPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> HabitsFragment()
                1 -> MoodFragment()
                2 -> HydrationTrackingFragment() // New simplified fragment
                else -> HabitsFragment()
            }
        }
    }
    
}

/**
 * HydrationTrackingFragment - Track daily water intake
 */
class HydrationTrackingFragment : Fragment() {

    private lateinit var sharedPreferences: android.content.SharedPreferences
    private lateinit var waterLevelCircle: WaterLevelCircleView
    private lateinit var waterGoalText: android.widget.TextView
    private lateinit var waterAmountInput: com.google.android.material.textfield.TextInputEditText
    private lateinit var addWaterButton: com.google.android.material.button.MaterialButton
    private lateinit var addSmallCupButton: com.google.android.material.button.MaterialButton
    private lateinit var addMediumCupButton: com.google.android.material.button.MaterialButton
    private lateinit var addLargeCupButton: com.google.android.material.button.MaterialButton
    private lateinit var customizeCupsButton: com.google.android.material.button.MaterialButton
    private lateinit var resetButton: com.google.android.material.button.MaterialButton
    private lateinit var historyRecyclerView: androidx.recyclerview.widget.RecyclerView
    
    // Reminder UI elements
    private lateinit var testNotificationButton: com.google.android.material.button.MaterialButton
    private lateinit var reminderToggleButton: com.google.android.material.button.MaterialButton
    private lateinit var reminderIntervalInput: com.google.android.material.textfield.TextInputEditText
    private lateinit var intervalTypeButton: com.google.android.material.button.MaterialButton
    private lateinit var reminderStatusText: android.widget.TextView
    
    private var dailyGoal = 2000 // ml (2L)
    private var currentWaterAmount = 0 // ml
    private val historyList = mutableListOf<WaterEntry>()
    
    // Customizable cup amounts
    private var smallCupAmount = 200 // ml
    private var mediumCupAmount = 300 // ml
    private var largeCupAmount = 500 // ml
    
    // Reminder state
    private var isReminderActive = false
    private var currentIntervalType = "minutes"
    private var reminderHandler: android.os.Handler? = null
    private var reminderRunnable: Runnable? = null
    private var lastReminderTime = 0L // "seconds", "minutes", "hours"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hydration_tracking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupSharedPreferences()
        loadWaterData()
        setupClickListeners()
        setupRecyclerView()
        updateCupButtons()
        updateReminderUI()
    }

    private fun initializeViews(view: View) {
        waterLevelCircle = view.findViewById(R.id.waterLevelCircle)
        waterGoalText = view.findViewById(R.id.waterGoalText)
        waterAmountInput = view.findViewById(R.id.waterAmountInput)
        addWaterButton = view.findViewById(R.id.addWaterButton)
        addSmallCupButton = view.findViewById(R.id.addSmallCupButton)
        addMediumCupButton = view.findViewById(R.id.addMediumCupButton)
        addLargeCupButton = view.findViewById(R.id.addLargeCupButton)
        customizeCupsButton = view.findViewById(R.id.customizeCupsButton)
        resetButton = view.findViewById(R.id.resetButton)
        historyRecyclerView = view.findViewById(R.id.historyRecyclerView)
        
        // Initialize reminder UI elements
        testNotificationButton = view.findViewById(R.id.testNotificationButton)
        reminderToggleButton = view.findViewById(R.id.reminderToggleButton)
        reminderIntervalInput = view.findViewById(R.id.reminderIntervalInput)
        intervalTypeButton = view.findViewById(R.id.intervalTypeButton)
        reminderStatusText = view.findViewById(R.id.reminderStatusText)
    }

    private fun setupSharedPreferences() {
        sharedPreferences = requireContext().getSharedPreferences("aquaglow_prefs", android.content.Context.MODE_PRIVATE)
        dailyGoal = sharedPreferences.getInt("water_daily_goal_ml", 2000)
    }

    private fun loadWaterData() {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        currentWaterAmount = sharedPreferences.getInt("water_intake_ml_$today", 0)
        
        // Load history
        val historyJson = sharedPreferences.getString("water_history_$today", "[]")
        val type = object : com.google.gson.reflect.TypeToken<List<WaterEntry>>() {}.type
        historyList.clear()
        historyList.addAll(com.google.gson.Gson().fromJson(historyJson, type) ?: emptyList())
        
        updateUI()
    }

    private fun setupClickListeners() {
        addWaterButton.setOnClickListener {
            addWaterFromInput()
        }
        
        addSmallCupButton.setOnClickListener {
            addWater(getSmallCupAmount())
        }
        
        addMediumCupButton.setOnClickListener {
            addWater(getMediumCupAmount())
        }
        
        addLargeCupButton.setOnClickListener {
            addWater(getLargeCupAmount())
        }
        
        customizeCupsButton.setOnClickListener {
            showCustomizeCupsDialog()
        }
        
        // Add goal customization click listener
        waterGoalText.setOnClickListener {
            showCustomizeGoalDialog()
        }
        
        resetButton.setOnClickListener {
            showResetConfirmation()
        }
        
        // Reminder click listeners
        testNotificationButton.setOnClickListener {
            showTestNotification()
        }
        
        reminderToggleButton.setOnClickListener {
            toggleReminder()
        }
        
        intervalTypeButton.setOnClickListener {
            showIntervalTypeDialog()
        }
    }

    private fun setupRecyclerView() {
        historyRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        historyRecyclerView.adapter = WaterHistoryAdapter(historyList)
    }

    private fun addWater(amount: Int) {
        currentWaterAmount += amount
        
        // Add to history
        val entry = WaterEntry(
            amount = amount,
            timestamp = System.currentTimeMillis()
        )
        historyList.add(0, entry)
        
        // Reset reminder timer when user drinks water
        lastReminderTime = System.currentTimeMillis()
        
        saveWaterData()
        updateUI()
        
        // Show toast
        android.widget.Toast.makeText(
            requireContext(),
            "Added ${amount}ml of water! 💧",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun addWaterFromInput() {
        val inputText = waterAmountInput.text.toString().trim()
        if (inputText.isEmpty()) {
            android.widget.Toast.makeText(
                requireContext(),
                "Please enter an amount",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }
        
        val amount = inputText.toIntOrNull() ?: 0
        if (amount <= 0) {
            android.widget.Toast.makeText(
                requireContext(),
                "Please enter a valid amount",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }
        
        addWater(amount)
        waterAmountInput.text?.clear()
    }

    private fun saveWaterData() {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        sharedPreferences.edit().apply {
            putInt("water_intake_ml_$today", currentWaterAmount)
            putString("water_history_$today", com.google.gson.Gson().toJson(historyList))
            apply()
        }
        
        // Refresh widget when water data changes
        refreshWidget()
    }
    
    private fun refreshWidget() {
        try {
            val intent = Intent(requireContext(), AquaGlowWidgetProvider::class.java).apply {
                action = AquaGlowWidgetProvider.ACTION_WIDGET_UPDATE
            }
            requireContext().sendBroadcast(intent)
        } catch (e: Exception) {
            android.util.Log.e("HydrationFragment", "Error refreshing widget", e)
        }
    }

    private fun updateUI() {
        waterGoalText.text = "Goal: ${dailyGoal}ml (${dailyGoal/1000}L) - Tap to customize"
        
        val percentage = (currentWaterAmount.toFloat() / dailyGoal).coerceAtMost(1f)
        waterLevelCircle.setWaterLevel(percentage)
        
        // Update RecyclerView
        historyRecyclerView.adapter?.notifyDataSetChanged()
    }

    private fun getSmallCupAmount(): Int {
        return sharedPreferences.getInt("small_cup_amount", 200)
    }
    
    private fun getMediumCupAmount(): Int {
        return sharedPreferences.getInt("medium_cup_amount", 300)
    }
    
    private fun getLargeCupAmount(): Int {
        return sharedPreferences.getInt("large_cup_amount", 500)
    }
    
    private fun showCustomizeCupsDialog() {
        val dialogView = android.view.LayoutInflater.from(requireContext())
            .inflate(android.R.layout.simple_list_item_1, null)
        
        val smallCupInput = android.widget.EditText(requireContext())
        smallCupInput.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        smallCupInput.hint = "Small cup (ml)"
        smallCupInput.setText(getSmallCupAmount().toString())
        
        val mediumCupInput = android.widget.EditText(requireContext())
        mediumCupInput.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        mediumCupInput.hint = "Medium cup (ml)"
        mediumCupInput.setText(getMediumCupAmount().toString())
        
        val largeCupInput = android.widget.EditText(requireContext())
        largeCupInput.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        largeCupInput.hint = "Large cup (ml)"
        largeCupInput.setText(getLargeCupAmount().toString())
        
        val layout = android.widget.LinearLayout(requireContext())
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 20)
        layout.addView(smallCupInput)
        layout.addView(mediumCupInput)
        layout.addView(largeCupInput)
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Customize Cup Sizes")
            .setMessage("Set the amount for each cup size (in ml):")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val smallAmount = smallCupInput.text.toString().toIntOrNull() ?: 200
                val mediumAmount = mediumCupInput.text.toString().toIntOrNull() ?: 300
                val largeAmount = largeCupInput.text.toString().toIntOrNull() ?: 500
                
                if (smallAmount > 0 && mediumAmount > 0 && largeAmount > 0) {
                    saveCupAmounts(smallAmount, mediumAmount, largeAmount)
                    updateCupButtons()
                    android.widget.Toast.makeText(requireContext(), "Cup sizes updated!", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(requireContext(), "Please enter valid amounts", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun saveCupAmounts(small: Int, medium: Int, large: Int) {
        sharedPreferences.edit().apply {
            putInt("small_cup_amount", small)
            putInt("medium_cup_amount", medium)
            putInt("large_cup_amount", large)
            apply()
        }
        smallCupAmount = small
        mediumCupAmount = medium
        largeCupAmount = large
    }
    
    private fun updateCupButtons() {
        addSmallCupButton.text = "${getSmallCupAmount()}ml"
        addMediumCupButton.text = "${getMediumCupAmount()}ml"
        addLargeCupButton.text = "${getLargeCupAmount()}ml"
    }

    private fun showResetConfirmation() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Reset Water Intake")
            .setMessage("Are you sure you want to reset today's water intake?")
            .setPositiveButton("Reset") { _, _ ->
                currentWaterAmount = 0
                historyList.clear()
                saveWaterData()
                updateUI()
                android.widget.Toast.makeText(requireContext(), "Water intake reset!", android.widget.Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    data class WaterEntry(
        val amount: Int, // in ml
        val timestamp: Long
    )

    private class WaterHistoryAdapter(private val entries: List<WaterEntry>) :
        androidx.recyclerview.widget.RecyclerView.Adapter<WaterHistoryAdapter.ViewHolder>() {

        class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            val timeText: android.widget.TextView = view.findViewById(R.id.timeText)
            val amountText: android.widget.TextView = view.findViewById(R.id.amountText)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_water_history, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val entry = entries[position]
            val dateTimeFormat = java.text.SimpleDateFormat("MMM dd, hh:mm a", java.util.Locale.getDefault())
            holder.timeText.text = dateTimeFormat.format(java.util.Date(entry.timestamp))
            holder.amountText.text = "+${entry.amount}ml"
        }

        override fun getItemCount() = entries.size
    }
    
    // Reminder methods
    private fun toggleReminder() {
        if (isReminderActive) {
            stopReminder()
        } else {
            startReminder()
        }
    }
    
    private fun startReminder() {
        val intervalText = reminderIntervalInput.text.toString()
        val interval = intervalText.toLongOrNull() ?: 30
        
        if (interval <= 0) {
            android.widget.Toast.makeText(requireContext(), "Please enter a valid interval", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        
        // Start in-app reminders
        startInAppReminders(interval)
        
        // Also start background service for when app is closed
        try {
            val intent = android.content.Intent(requireContext(), HydrationReminderService::class.java).apply {
                action = HydrationReminderService.ACTION_START_REMINDER
                putExtra(HydrationReminderService.EXTRA_INTERVAL, interval)
                putExtra(HydrationReminderService.EXTRA_INTERVAL_TYPE, currentIntervalType)
            }
            
            requireContext().startForegroundService(intent)
        } catch (e: Exception) {
            android.util.Log.e("HydrationReminder", "Failed to start reminder service", e)
            android.widget.Toast.makeText(requireContext(), "Failed to start background reminders: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
        
        isReminderActive = true
        updateReminderUI()
        
        android.widget.Toast.makeText(requireContext(), "Hydration reminders started! You'll get reminders while using the app.", android.widget.Toast.LENGTH_LONG).show()
    }
    
    private fun startInAppReminders(interval: Long) {
        // Convert interval to milliseconds
        val intervalMs = when (currentIntervalType) {
            "seconds" -> interval * 1000
            "minutes" -> interval * 60 * 1000
            "hours" -> interval * 60 * 60 * 1000
            else -> interval * 60 * 1000 // default to minutes
        }
        
        // Stop any existing reminders
        stopInAppReminders()
        
        // Create new handler
        reminderHandler = android.os.Handler(android.os.Looper.getMainLooper())
        
        reminderRunnable = object : Runnable {
            override fun run() {
                android.util.Log.d("HydrationReminder", "Reminder runnable executed - isReminderActive: $isReminderActive")
                if (isReminderActive) {
                    showInAppReminder()
                    android.util.Log.d("HydrationReminder", "Scheduling next reminder in ${intervalMs}ms")
                    reminderHandler?.postDelayed(this, intervalMs)
                } else {
                    android.util.Log.d("HydrationReminder", "Reminder stopped - not scheduling next reminder")
                }
            }
        }
        
        // Start the first reminder after a short delay
        android.util.Log.d("HydrationReminder", "Starting in-app reminders every ${intervalMs}ms (${interval} ${currentIntervalType})")
        reminderHandler?.postDelayed(reminderRunnable!!, intervalMs)
    }
    
    private fun stopInAppReminders() {
        reminderRunnable?.let { 
            reminderHandler?.removeCallbacks(it)
        }
        reminderRunnable = null
        reminderHandler = null
    }
    
    private fun showInAppReminder() {
        val currentTime = System.currentTimeMillis()
        
        // Don't show reminder if user just drank water (very short cooldown for very short intervals)
        val interval = reminderIntervalInput.text.toString().toLongOrNull() ?: 30
        val cooldownTime = when {
            currentIntervalType == "seconds" && interval <= 5 -> 5 * 1000 // 5 seconds for very short intervals
            currentIntervalType == "seconds" && interval <= 30 -> 10 * 1000 // 10 seconds for short intervals
            else -> 2 * 60 * 1000 // 2 minutes for longer intervals
        }
        
        if (currentTime - lastReminderTime < cooldownTime) {
            android.util.Log.d("HydrationReminder", "Skipping reminder - cooldown active (${(cooldownTime - (currentTime - lastReminderTime))/1000}s remaining)")
            return
        }
        
        // Calculate how much water user should have had by now
        val hoursSinceStart = (currentTime - getStartOfDay()) / (1000 * 60 * 60)
        val expectedWater = (hoursSinceStart * dailyGoal / 16).toInt() // Assume 16 waking hours
        
        val messages = when {
            currentWaterAmount == 0 -> arrayOf(
                "💧 You haven't had any water today! Time to start hydrating!",
                "🚰 Your body needs water to function properly. Take a sip!",
                "💦 Dehydration can affect your mood and energy. Drink up!"
            )
            currentWaterAmount < expectedWater / 2 -> arrayOf(
                "⚠️ You're behind on your water intake! Catch up now!",
                "💧 Your body is asking for water. Don't ignore it!",
                "🚰 You're missing your water goals. Time to hydrate!"
            )
            currentWaterAmount < expectedWater -> arrayOf(
                "💧 You're a bit behind on hydration. Keep drinking!",
                "🚰 Your body needs more water. Take a break and drink!",
                "💦 Stay on track with your water goals!"
            )
            else -> arrayOf(
                "💧 Great job staying hydrated! Keep it up!",
                "🚰 You're doing well with water intake. Don't stop now!",
                "💦 Excellent hydration habits! Your body thanks you!"
            )
        }
        
        val randomMessage = messages.random()
        
        // Log the reminder
        android.util.Log.d("HydrationReminder", "Showing reminder: $randomMessage")
        
        // Show as a prominent toast
        val toast = android.widget.Toast.makeText(requireContext(), randomMessage, android.widget.Toast.LENGTH_LONG)
        toast.setGravity(android.view.Gravity.CENTER, 0, 0)
        toast.show()
        
        // Also show as notification if app is in background
        showBackgroundNotification(randomMessage)
        
        lastReminderTime = currentTime
    }
    
    private fun getStartOfDay(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    private fun showBackgroundNotification(message: String) {
        try {
            val notificationManager = requireContext().getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            
            val intent = android.content.Intent(requireContext(), com.example.aquaglow.MainActivity::class.java).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = android.app.PendingIntent.getActivity(
                requireContext(), 0, intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val notification = androidx.core.app.NotificationCompat.Builder(requireContext(), "hydration_reminders")
                .setContentTitle("💧 AquaGlow Reminder")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_water_drop)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(androidx.core.app.NotificationCompat.CATEGORY_REMINDER)
                .setVibrate(longArrayOf(0, 300, 100, 300))
                .build()

            notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
        } catch (e: Exception) {
            android.util.Log.e("InAppReminder", "Failed to show background notification", e)
        }
    }
    
    private fun stopReminder() {
        // Stop in-app reminders
        stopInAppReminders()
        
        // Stop background service
        val intent = android.content.Intent(requireContext(), HydrationReminderService::class.java).apply {
            action = HydrationReminderService.ACTION_STOP_REMINDER
        }
        
        requireContext().startService(intent)
        
        isReminderActive = false
        updateReminderUI()
        
        android.widget.Toast.makeText(requireContext(), "Hydration reminders stopped!", android.widget.Toast.LENGTH_SHORT).show()
    }
    
    private fun showTestNotification() {
        try {
            val notificationManager = requireContext().getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            
            // Create intent to open the app
            val intent = android.content.Intent(requireContext(), com.example.aquaglow.MainActivity::class.java).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = android.app.PendingIntent.getActivity(
                requireContext(), 0, intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val notification = androidx.core.app.NotificationCompat.Builder(requireContext(), "hydration_reminders")
                .setContentTitle("💧 Test Hydration Reminder!")
                .setContentText("This is a test notification. Click to open AquaGlow app!")
                .setSmallIcon(R.drawable.ic_water_drop)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setCategory(androidx.core.app.NotificationCompat.CATEGORY_REMINDER)
                .setVibrate(longArrayOf(0, 300, 100, 300))
                .build()

            notificationManager.notify(9999, notification)
            
            android.widget.Toast.makeText(requireContext(), "Test notification sent! Check your notification panel.", android.widget.Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            android.util.Log.e("TestNotification", "Failed to show test notification", e)
            android.widget.Toast.makeText(requireContext(), "Failed to show test notification: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }
    
    private fun updateReminderUI() {
        if (isReminderActive) {
            reminderToggleButton.text = "Stop"
            reminderToggleButton.backgroundTintList = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.error))
            reminderStatusText.text = "Reminders are active - every ${reminderIntervalInput.text} ${currentIntervalType}"
        } else {
            reminderToggleButton.text = "Start"
            reminderToggleButton.backgroundTintList = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.primary))
            reminderStatusText.text = "Reminders are off"
        }
    }
    
    private fun showIntervalTypeDialog() {
        val options = arrayOf("Seconds", "Minutes", "Hours")
        val currentIndex = when (currentIntervalType) {
            "seconds" -> 0
            "minutes" -> 1
            "hours" -> 2
            else -> 1
        }
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Interval Type")
            .setSingleChoiceItems(options, currentIndex) { dialog, which ->
                currentIntervalType = when (which) {
                    0 -> "seconds"
                    1 -> "minutes"
                    2 -> "hours"
                    else -> "minutes"
                }
                intervalTypeButton.text = options[which]
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        loadWaterData()
        updateUI()
        updateCupButtons()
        updateReminderUI()
    }
    
    override fun onPause() {
        super.onPause()
        // Don't stop reminders when pausing - let them continue
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Stop reminders when fragment is destroyed
        stopInAppReminders()
    }
    
    private fun showCustomizeGoalDialog() {
        val dialogView = android.view.LayoutInflater.from(requireContext())
            .inflate(android.R.layout.simple_list_item_1, null)
        
        val goalInput = android.widget.EditText(requireContext())
        goalInput.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        goalInput.hint = "Daily water goal (ml)"
        goalInput.setText(dailyGoal.toString())
        
        val layout = android.widget.LinearLayout(requireContext())
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 20)
        layout.addView(goalInput)
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Customize Daily Goal")
            .setMessage("Set your daily water intake goal (in ml):")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val newGoal = goalInput.text.toString().toIntOrNull() ?: 2000
                
                if (newGoal > 0 && newGoal <= 10000) { // Reasonable range: 0-10L
                    dailyGoal = newGoal
                    sharedPreferences.edit().putInt("water_daily_goal_ml", dailyGoal).apply()
                    updateUI()
                    android.widget.Toast.makeText(requireContext(), "Daily goal updated to ${dailyGoal}ml!", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(requireContext(), "Please enter a valid goal (1-10000ml)", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

