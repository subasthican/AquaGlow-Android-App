package com.example.aquaglow

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial

/**
 * SettingsFragment provides app configuration and user preferences
 */
class SettingsFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var themeSwitch: SwitchMaterial
    private lateinit var hydrationIntervalSpinner: Spinner
    private lateinit var clearDataButton: MaterialButton
    private lateinit var appVersionText: TextView
    private lateinit var developerText: TextView
    
    // Sensor tracking views
    private lateinit var sensorTrackingSwitch: SwitchMaterial
    private lateinit var stepGoalInput: EditText
    private lateinit var activityStatsLayout: LinearLayout
    private lateinit var stepCountText: TextView
    private lateinit var shakeCountText: TextView
    private lateinit var themeSpinner: Spinner
    private lateinit var testNotificationButton: MaterialButton
    private lateinit var logoutButton: MaterialButton
    private lateinit var deleteAccountButton: MaterialButton
    
    // Flags to prevent auto-triggering during initialization
    private var isInitializingSpinners = true
    
    // Handler for dynamic updates
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val updateStatsRunnable = object : Runnable {
        override fun run() {
            updateActivityStats()
            handler.postDelayed(this, 1000) // Update every 1 second
        }
    }
    
    // Permission launcher for Android 10+ (API 29+)
    private val activityPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, start the service
            SensorManager.setSensorTrackingEnabled(requireContext(), true)
            updateActivityStatsVisibility()
            // Start dynamic updates
            handler.post(updateStatsRunnable)
            Toast.makeText(requireContext(), getString(R.string.settings_activity_enabled), Toast.LENGTH_SHORT).show()
        } else {
            // Permission denied
            sensorTrackingSwitch.isChecked = false
            Toast.makeText(
                requireContext(),
                getString(R.string.settings_activity_permission_required),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup back button
        setupBackButton(view)

        initializeViews(view)
        setupSharedPreferences()
        setupThemeSwitch()
        setupHydrationInterval()
        setupSensorTracking()
        setupThemeSelection()
        setupClearDataButton()
        setupAppInfoButtons()
        setupTestNotificationButton()
        setupLogoutButton()
        setupDeleteAccountButton()
        setupGlowEffects(view)
        
        // Done initializing, allow spinner events
        isInitializingSpinners = false
    }
    
    /**
     * Setup back button navigation
     */
    private fun setupBackButton(view: View) {
        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            // Go back to Profile page
            findNavController().navigateUp()
        }
    }

    private fun initializeViews(view: View) {
        themeSwitch = view.findViewById(R.id.themeSwitch)
        hydrationIntervalSpinner = view.findViewById(R.id.hydrationIntervalSpinner)
        clearDataButton = view.findViewById(R.id.clearDataButton)
        appVersionText = view.findViewById(R.id.appVersionText)
        developerText = view.findViewById(R.id.developerText)
        
        // Sensor tracking views
        sensorTrackingSwitch = view.findViewById(R.id.sensorTrackingSwitch)
        stepGoalInput = view.findViewById(R.id.stepGoalInput)
        activityStatsLayout = view.findViewById(R.id.activityStatsLayout)
        stepCountText = view.findViewById(R.id.stepCountText)
        shakeCountText = view.findViewById(R.id.shakeCountText)
        themeSpinner = view.findViewById(R.id.themeSpinner)
        testNotificationButton = view.findViewById(R.id.testNotificationButton)
        logoutButton = view.findViewById(R.id.logoutButton)
        deleteAccountButton = view.findViewById(R.id.deleteAccountButton)
    }

    private fun setupSharedPreferences() {
        sharedPreferences = requireContext().getSharedPreferences("aquaglow_prefs", 0)
    }

    private fun setupThemeSwitch() {
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
        themeSwitch.isChecked = isDarkMode

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Ignore if initializing
            if (isInitializingSpinners) return@setOnCheckedChangeListener
            
            val previousMode = sharedPreferences.getBoolean("dark_mode", false)
            
            // Only apply if actually changed
            if (isChecked != previousMode) {
                sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply()
                
                // Apply theme change - this recreates the activity to apply theme consistently
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                
                // Note: Activity will recreate, so toast and navigation state will be reset
                // This is normal behavior for theme changes
            }
        }
    }

    private fun setupHydrationInterval() {
        val intervals = arrayOf("30 minutes", "1 hour", "2 hours", "3 hours", "4 hours", "6 hours")
        val intervalValues = arrayOf(30, 60, 120, 180, 240, 360)
        
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, intervals)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        hydrationIntervalSpinner.adapter = adapter

        val currentInterval = sharedPreferences.getInt("hydration_interval_minutes", 120)
        val selectedIndex = intervalValues.indexOf(currentInterval)
        if (selectedIndex >= 0) {
            hydrationIntervalSpinner.setSelection(selectedIndex)
        }

        hydrationIntervalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Ignore if initializing
                if (isInitializingSpinners) return
                
                val selectedInterval = intervalValues[position]
                val previousInterval = sharedPreferences.getInt("hydration_interval_minutes", 120)
                
                // Only update if value actually changed
                if (selectedInterval != previousInterval) {
                sharedPreferences.edit().putInt("hydration_interval_minutes", selectedInterval).apply()
                
                // Schedule hydration reminders with new interval
                WorkManagerUtils.scheduleHydrationReminder(requireContext(), selectedInterval)
                    Toast.makeText(requireContext(), String.format(getString(R.string.settings_hydration_set), intervals[position]), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupThemeSelection() {
        val themes = ThemeManager.getAllThemes()
        val themeNames = themes.map { it.displayName }.toTypedArray()

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, themeNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        themeSpinner.adapter = adapter

        val currentTheme = ThemeManager.getCurrentTheme(requireContext())
        val selectedIndex = themes.indexOf(currentTheme)
        if (selectedIndex >= 0) {
            themeSpinner.setSelection(selectedIndex)
        }
        
        // Disable theme selector temporarily - feature not fully implemented yet
        themeSpinner.isEnabled = false
        themeSpinner.alpha = 0.5f

        themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Ignore if initializing (prevents auto-showing message on page load)
                if (isInitializingSpinners) return
                
                // Theme selection disabled - feature under development
                // The colors are defined but not yet applied to the UI dynamically
                // Only show message if user actually tries to interact with it
                Toast.makeText(
                    requireContext(), 
                    getString(R.string.settings_theme_coming_soon), 
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupClearDataButton() {
        clearDataButton.setOnClickListener {
            showClearDataDialog()
        }
    }
    
    private fun setupTestNotificationButton() {
        testNotificationButton.setOnClickListener {
            showTestNotificationMenu()
        }
    }
    
    private fun setupLogoutButton() {
        // Only show logout button if user is logged in
        if (!AuthUtils.isLoggedIn(requireContext())) {
            logoutButton.visibility = View.GONE
            return
        }
        
        // Simple logout - just logs you out while keeping account for re-login
        logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }
    }
    
    private fun setupDeleteAccountButton() {
        // Only show delete account button if user is logged in
        if (!AuthUtils.isLoggedIn(requireContext())) {
            deleteAccountButton.visibility = View.GONE
            return
        }
        
        // Delete account - permanently removes all account data
        deleteAccountButton.setOnClickListener {
            showDeleteAccountConfirmation()
        }
    }
    
    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?\n\n✓ Your account will be saved\n✓ You can login again anytime")
            .setPositiveButton("Logout") { _, _ ->
                performLogout(keepAccount = true)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showDeleteAccountConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("⚠️ Delete Account?")
            .setMessage("This will PERMANENTLY delete your account and all data!\n\n❌ Cannot be undone\n❌ All progress will be lost\n\nAre you absolutely sure?")
            .setPositiveButton("Delete Forever") { _, _ ->
                performLogout(keepAccount = false)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun performLogout(keepAccount: Boolean) {
        // CRITICAL: Force clear sensor data first
        SensorManager.forceClearSensorData(requireContext())
        
        if (keepAccount) {
            // Just logout, keep credentials for re-login
            AuthUtils.logout(requireContext())
            Toast.makeText(requireContext(), getString(R.string.logout_success), Toast.LENGTH_LONG).show()
        } else {
            // Delete account - clear ALL data including sensor data
            val sp = requireContext().getSharedPreferences("aquaglow_prefs", Context.MODE_PRIVATE)
            sp.edit().clear().commit()  // Use commit() to force immediate save
            
            Toast.makeText(requireContext(), getString(R.string.delete_account_success), Toast.LENGTH_LONG).show()
        }
        
        // Navigate to login screen
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
    
    private fun showTestNotificationMenu() {
        val notificationTypes = arrayOf(
            "Hydration Reminder",
            "Step Milestone",
            "Shake Mood Suggestion",
            "Test Notification"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("Test Notification")
            .setItems(notificationTypes) { _, which ->
                when (which) {
                    0 -> testHydrationNotification()
                    1 -> testStepMilestoneNotification()
                    2 -> testShakeMoodNotification()
                    3 -> testGeneralNotification()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun testHydrationNotification() {
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create channel if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "hydration_channel",
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to stay hydrated"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val intent = Intent(requireContext(), MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(requireContext(), "hydration_channel")
            .setContentTitle("💧 Time to drink water!")
            .setContentText("Stay hydrated and keep your body healthy")
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        notificationManager.notify(3001, notification)
        Toast.makeText(requireContext(), getString(R.string.settings_test_hydration), Toast.LENGTH_SHORT).show()
    }
    
    private fun testStepMilestoneNotification() {
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val intent = Intent(requireContext(), MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(requireContext(), "step_counter_channel")
            .setContentTitle("🎉 Step Milestone!")
            .setContentText("You've reached 5000 steps! Great job staying active!")
            .setSmallIcon(R.drawable.ic_trending_up)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        notificationManager.notify(3002, notification)
        Toast.makeText(requireContext(), getString(R.string.settings_test_step), Toast.LENGTH_SHORT).show()
    }
    
    private fun testShakeMoodNotification() {
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            putExtra("open_mood_fragment", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(requireContext(), "step_counter_channel")
            .setContentTitle("💭 How are you feeling?")
            .setContentText("You shook your device - want to log your mood?")
            .setSmallIcon(R.drawable.ic_mood)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        notificationManager.notify(3003, notification)
        Toast.makeText(requireContext(), getString(R.string.settings_test_shake), Toast.LENGTH_SHORT).show()
    }
    
    private fun testGeneralNotification() {
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val intent = Intent(requireContext(), MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(requireContext(), "test_channel")
            .setContentTitle("🔔 Test Notification")
            .setContentText("This is a test notification from AquaGlow!")
            .setSmallIcon(R.drawable.ic_star)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        notificationManager.notify(3004, notification)
        Toast.makeText(requireContext(), "Test notification sent!", Toast.LENGTH_SHORT).show()
    }

    private fun setupAppInfoButtons() {
        // About button
        view?.findViewById<View>(R.id.aboutButton)?.setOnClickListener {
            val intent = Intent(requireContext(), AboutActivity::class.java)
            startActivity(intent)
        }

        // Help button
        view?.findViewById<View>(R.id.helpButton)?.setOnClickListener {
            val intent = Intent(requireContext(), HelpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showClearDataDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear All Data")
            .setMessage("This will reset the app to fresh state:\n\n✓ Clear habits, moods, statistics\n✓ Reset steps and shakes to 0\n✓ Clear all achievements\n✓ Keep you logged in\n\nLike a new app but still your account. Continue?")
            .setPositiveButton("Clear All") { _, _ ->
                clearAllData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearAllData() {
        // Show progress
        val progressDialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Clearing Data...")
            .setMessage("Please wait...")
            .setCancelable(false)
            .create()
        progressDialog.show()
        
        // CRITICAL: Force clear sensor data FIRST (stops service and clears counts)
        SensorManager.forceClearSensorData(requireContext())
        
        // Wait a moment for service to fully stop
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            try {
                // STEP 1: Preserve ONLY authentication data
                val authEmail = AuthUtils.getUserEmail(requireContext())
                val authName = AuthUtils.getUserName(requireContext())
                val authPasswordHash = sharedPreferences.getString("auth_user_password_hash", null)
                val isLoggedIn = AuthUtils.isLoggedIn(requireContext())
                
                // STEP 2: Clear EVERYTHING (use commit() for immediate save)
                sharedPreferences.edit().clear().commit()
                
                // STEP 3: Restore ONLY authentication + essential settings
                val editor = sharedPreferences.edit()
                
                // Restore authentication (keep logged in)
                if (authEmail != null && authName != null && authPasswordHash != null && isLoggedIn) {
                    editor.putString("auth_user_email", authEmail)
                        .putString("auth_user_name", authName)
                        .putString("auth_user_password_hash", authPasswordHash)
                        .putBoolean("auth_logged_in", true)
                        .putString("user_name", authName)
                } else {
                    editor.putString("user_name", "User")
                }
                
                // Set default settings (fresh app state)
                editor.putBoolean("onboarding_completed", true)
            .putBoolean("dark_mode", false)
            .putInt("hydration_interval_minutes", 120)
                    .putInt("daily_step_goal", 10000)
                    
                    // IMPORTANT: Reset all data to 0/empty
                    .putInt("step_count", 0)
                    .putInt("shake_count", 0)
                    .putBoolean("sensor_tracking_enabled", false)
                    .putString("habits_list", "[]")
                    .putString("mood_entries", "[]")
                    .putString("achievements", "[]")
                    .putString("streaks", "[]")
                    .putString("analytics_data", "[]")
                    .putString("health_metrics", "[]")
                    .putString("game_scores", "[]")
                    .putString("goals_list", "[]")
                    .putLong("last_activity_update", 0)
                    
                    .commit()  // Use commit() to force immediate save

                // STEP 4: Update UI to reflect cleared state
                sensorTrackingSwitch.isChecked = false
                updateActivityStatsVisibility()
                
                // Force update the displayed step and shake counts to 0
                stepCountText.text = "Steps: 0"
                shakeCountText.text = "Shakes: 0"
                
                // Dismiss progress dialog
                progressDialog.dismiss()
                
                // STEP 5: Show success message
                Toast.makeText(
                    requireContext(), 
                    getString(R.string.clear_data_success), 
                    Toast.LENGTH_LONG
                ).show()
                
            } catch (e: Exception) {
                progressDialog.dismiss()
                Toast.makeText(requireContext(), String.format(getString(R.string.clear_data_error), e.message), Toast.LENGTH_LONG).show()
            }
        }, 500) // Wait 500ms for service to stop
    }

    private fun setupSensorTracking() {
        // Check if sensors are available
        if (!SensorManager.areSensorsAvailable(requireContext())) {
            sensorTrackingSwitch.isEnabled = false
            sensorTrackingSwitch.text = "Sensors not available on this device"
            return
        }
        
        // Load current settings
        val isEnabled = SensorManager.isSensorTrackingEnabled(requireContext())
        sensorTrackingSwitch.isChecked = isEnabled
        
        val stepGoal = SensorManager.getDailyStepGoal(requireContext())
        stepGoalInput.setText(stepGoal.toString())
        
        // Update activity stats visibility
        updateActivityStatsVisibility()
        
        // Set up switch listener
        sensorTrackingSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Check and request permission if needed
                if (checkActivityPermission()) {
                    // Permission already granted
                    SensorManager.setSensorTrackingEnabled(requireContext(), true)
                    updateActivityStatsVisibility()
                    // Start dynamic updates
                    handler.post(updateStatsRunnable)
                Toast.makeText(requireContext(), getString(R.string.settings_activity_enabled), Toast.LENGTH_SHORT).show()
                } else {
                    // Request permission
                    requestActivityPermission()
                }
            } else {
                // Disable tracking
                SensorManager.setSensorTrackingEnabled(requireContext(), false)
                updateActivityStatsVisibility()
                // Stop dynamic updates
                handler.removeCallbacks(updateStatsRunnable)
                Toast.makeText(requireContext(), getString(R.string.settings_activity_disabled), Toast.LENGTH_SHORT).show()
            }
        }
        
        // Set up step goal listener
        stepGoalInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                try {
                    val goal = stepGoalInput.text.toString().toInt()
                    if (goal > 0) {
                        SensorManager.setDailyStepGoal(requireContext(), goal)
                        Toast.makeText(requireContext(), String.format(getString(R.string.settings_step_goal_updated), goal), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: NumberFormatException) {
                    stepGoalInput.setText(SensorManager.getDailyStepGoal(requireContext()).toString())
                    Toast.makeText(requireContext(), getString(R.string.settings_invalid_step_goal), Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        // Update stats periodically
        updateActivityStats()
    }
    
    private fun updateActivityStatsVisibility() {
        val isEnabled = SensorManager.isSensorTrackingEnabled(requireContext())
        activityStatsLayout.visibility = if (isEnabled) View.VISIBLE else View.GONE
    }
    
    private fun updateActivityStats() {
        if (SensorManager.isSensorTrackingEnabled(requireContext())) {
            val stepCount = SensorManager.getCurrentStepCount(requireContext())
            val shakeCount = SensorManager.getCurrentShakeCount(requireContext())
            
            stepCountText.text = stepCount.toString()
            shakeCountText.text = shakeCount.toString()
        }
    }

    private fun setupAppInfo() {
        appVersionText.text = "Version 1.0.0"
        developerText.text = "Developed by AquaGlow Team"
    }
    
    override fun onResume() {
        super.onResume()
        // Start dynamic updates when fragment is visible
        if (SensorManager.isSensorTrackingEnabled(requireContext())) {
            handler.post(updateStatsRunnable)
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Stop dynamic updates when fragment is not visible to save battery
        handler.removeCallbacks(updateStatsRunnable)
    }

    private fun setupGlowEffects(view: View) {
        // Add breathing effect to clear data button
        GlowAnimationUtils.createBreathingEffect(clearDataButton, 4000L)
        
        // Add pulse effect to theme switch
        GlowAnimationUtils.createPulseEffect(themeSwitch, 3000L)
        
        // Add glow movement to app info text
        GlowAnimationUtils.createBreathingEffect(appVersionText, 4000L)
        GlowAnimationUtils.createBreathingEffect(developerText, 4000L)
        
        // Apply material glow effects
        GlowAnimationUtils.applyMaterialGlow(clearDataButton)
    }
    
    /**
     * Check if ACTIVITY_RECOGNITION permission is granted
     */
    private fun checkActivityPermission(): Boolean {
        // Android 10 (API 29) and above requires ACTIVITY_RECOGNITION permission
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Permission not required for Android 9 and below
            true
        }
    }
    
    /**
     * Request ACTIVITY_RECOGNITION permission for Android 10+
     */
    private fun requestActivityPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when {
                shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION) -> {
                    // Show explanation dialog
                    AlertDialog.Builder(requireContext())
                        .setTitle("Permission Required")
                        .setMessage("Activity tracking permission is needed to count your steps and detect shake gestures. This helps track your wellness activities.")
                        .setPositiveButton("Grant Permission") { _, _ ->
                            activityPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                        }
                        .setNegativeButton("Cancel") { _, _ ->
                            sensorTrackingSwitch.isChecked = false
                        }
                        .show()
                }
                else -> {
                    // Request permission
                    activityPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                }
            }
        }
    }
}