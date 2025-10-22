package com.example.aquaglow

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.app.TimePickerDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * HabitsFragment manages daily wellness habits with progress tracking
 */
class HabitsFragment : Fragment() {

    private lateinit var habitsRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var progressPercentage: TextView
    private lateinit var addHabitFab: FloatingActionButton
    private lateinit var habitsAdapter: HabitsAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var gson: Gson
    
    // History components

    private var habitsList = mutableListOf<Habit>()
    private val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    private var isDataLoaded = false
    private var justAddedHabit = false
    
    companion object {
        private var staticHabitsList = mutableListOf<Habit>()
        private var staticIsDataLoaded = false
        
        // Thread safety
        @Synchronized
        fun getStaticHabitsList(): MutableList<Habit> {
            return staticHabitsList.toMutableList()
        }
        
        @Synchronized
        fun setStaticHabitsList(habits: MutableList<Habit>) {
            staticHabitsList.clear()
            staticHabitsList.addAll(habits)
        }
        
        @Synchronized
        fun isStaticDataLoaded(): Boolean {
            return staticIsDataLoaded
        }
        
        @Synchronized
        fun setStaticDataLoaded(loaded: Boolean) {
            staticIsDataLoaded = loaded
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_habits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            android.util.Log.d("HabitsFragment", "onViewCreated() called")
            debugAllData()
            
            initializeViews(view)
            setupSharedPreferences()
            loadHabits()
            setupRecyclerView()
            setupFab()
            updateProgress()
            setupGlowEffects()
            
            android.util.Log.d("HabitsFragment", "onViewCreated() completed")
            debugAllData()
        } catch (e: Exception) {
            android.util.Log.e("HabitsFragment", "Error in onViewCreated: ${e.message}", e)
            // Initialize with empty data to prevent crashes
            habitsList = mutableListOf()
            if (::habitsAdapter.isInitialized) {
                habitsAdapter.updateHabits(habitsList)
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Only refresh habits if we haven't loaded data yet
        if (::sharedPreferences.isInitialized && !isDataLoaded) {
            android.util.Log.d("HabitsFragment", "onResume() - Loading habits because data not loaded")
            loadHabits()
        } else {
            android.util.Log.d("HabitsFragment", "onResume() - Skipping load because data already loaded")
        }
    }
    
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && !isDataLoaded) {
            // Fragment is now visible, refresh data only if not already loaded
            if (::sharedPreferences.isInitialized) {
                loadHabits()
            }
        }
    }

    private fun initializeViews(view: View) {
        habitsRecyclerView = view.findViewById(R.id.habitsRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        progressText = view.findViewById(R.id.progressText)
        progressPercentage = view.findViewById(R.id.progressPercentage)
        addHabitFab = view.findViewById(R.id.addHabitFab)
        
    }

    private fun setupSharedPreferences() {
        sharedPreferences = requireContext().getSharedPreferences("aquaglow_prefs", 0)
        gson = Gson()
    }

    private fun loadHabits() {
        try {
            android.util.Log.d("HabitsFragment", "loadHabits() called - isDataLoaded: $isDataLoaded, staticIsDataLoaded: $staticIsDataLoaded, justAddedHabit: $justAddedHabit")
            
            // If we just added a habit, don't reload from storage
            if (justAddedHabit) {
                android.util.Log.d("HabitsFragment", "Skipping load because we just added a habit")
                justAddedHabit = false
                return
            }
            
            // If static data is already loaded, use it instead of reloading from storage
            if (isStaticDataLoaded() && getStaticHabitsList().isNotEmpty()) {
                val staticHabits = getStaticHabitsList()
                android.util.Log.d("HabitsFragment", "Using static data: ${staticHabits.size} habits")
                
                // Only update if the lists are different
                if (habitsList.size != staticHabits.size || !habitsList.containsAll(staticHabits)) {
                    android.util.Log.d("HabitsFragment", "Updating habitsList from static data - Current: ${habitsList.size}, Static: ${staticHabits.size}")
                    
                    // Create a new list to avoid clearing the current one
                    val newHabitsList = mutableListOf<Habit>()
                    newHabitsList.addAll(staticHabits)
                    habitsList = newHabitsList
                    
                    android.util.Log.d("HabitsFragment", "After update: ${habitsList.size} habits in habitsList")
                    
                    // Update adapter if it exists
                    if (::habitsAdapter.isInitialized) {
                        android.util.Log.d("HabitsFragment", "Updating adapter with ${habitsList.size} habits")
                        habitsAdapter.updateHabits(habitsList)
                    } else {
                        android.util.Log.d("HabitsFragment", "Adapter not initialized, cannot update")
                    }
                } else {
                    android.util.Log.d("HabitsFragment", "habitsList already matches static data, skipping update")
                }
                
                // Restore today's completion progress
                restoreTodaysProgress()
                
                // Update progress after loading
                updateProgress()
                
                isDataLoaded = true
                return
            }
            
            val habitsJson = sharedPreferences.getString("habits_list", "[]")
            val type = object : TypeToken<List<Habit>>() {}.type
            val loadedHabits = gson.fromJson<List<Habit>>(habitsJson, type) ?: emptyList()
            
            // Debug logging
            android.util.Log.d("HabitsFragment", "Loading habits from storage: ${loadedHabits.size} habits found")
            loadedHabits.forEach { habit ->
                android.util.Log.d("HabitsFragment", "Loaded habit: ${habit.name} (${habit.id})")
            }
            
            // Migrate existing habits to new format
            val migratedHabits = loadedHabits.map { habit ->
                migrateHabitToNewFormat(habit)
            }
            
            // Calculate status for all habits
            migratedHabits.forEach { habit ->
                calculateHabitStatus(habit)
            }
            
            // Create new list instead of clearing
            val newHabitsList = mutableListOf<Habit>()
            newHabitsList.addAll(migratedHabits)
            habitsList = newHabitsList
            
            // Update static data
            setStaticHabitsList(migratedHabits.toMutableList())
            setStaticDataLoaded(true)
            
            // Restore today's completion progress
            restoreTodaysProgress()
            
            // Update adapter if it exists
            if (::habitsAdapter.isInitialized) {
                habitsAdapter.updateHabits(habitsList)
            }
            
            // Update progress after loading
            updateProgress()
            
            
            isDataLoaded = true
            
        } catch (e: Exception) {
            // If there's an error loading habits, initialize with empty list
            android.util.Log.e("HabitsFragment", "Error loading habits: ${e.message}", e)
            habitsList = mutableListOf()
            if (::habitsAdapter.isInitialized) {
                habitsAdapter.updateHabits(habitsList)
            }
            updateProgress()
            isDataLoaded = true
        }
    }
    
    private fun restoreTodaysProgress() {
        val progressKey = "habits_progress_$today"
        val progressJson = sharedPreferences.getString(progressKey, "{}")
        val type = object : TypeToken<Map<String, Boolean>>() {}.type
        val progress = gson.fromJson<Map<String, Boolean>>(progressJson, type) ?: emptyMap()
        
        // Update habit completion status from today's progress
        habitsList.forEach { habit ->
            habit.isCompleted = progress[habit.id] ?: false
        }
    }

    private fun saveHabits() {
        try {
            val habitsJson = gson.toJson(habitsList)
            val success = sharedPreferences.edit().putString("habits_list", habitsJson).commit()
            android.util.Log.d("HabitsFragment", "Saved ${habitsList.size} habits - Success: $success")
            
            // Update static data
            staticHabitsList.clear()
            staticHabitsList.addAll(habitsList)
            staticIsDataLoaded = true
            
            // Refresh widget when habits change
            refreshWidget()
            
            // Verify the save worked
            val savedJson = sharedPreferences.getString("habits_list", "[]")
            val savedHabits = gson.fromJson<List<Habit>>(savedJson, object : TypeToken<List<Habit>>() {}.type) ?: emptyList()
            android.util.Log.d("HabitsFragment", "Verification: ${savedHabits.size} habits in storage")
            
        } catch (e: Exception) {
            // Log error if needed
            android.util.Log.e("HabitsFragment", "Error saving habits: ${e.message}")
        }
    }
    
    private fun refreshWidget() {
        try {
            val intent = Intent(requireContext(), AquaGlowWidgetProvider::class.java).apply {
                action = AquaGlowWidgetProvider.ACTION_WIDGET_UPDATE
            }
            requireContext().sendBroadcast(intent)
        } catch (e: Exception) {
            android.util.Log.e("HabitsFragment", "Error refreshing widget", e)
        }
    }

    private fun setupRecyclerView() {
        habitsAdapter = HabitsAdapter(
            habitsList,
            { habit, isCompleted -> updateHabitProgress(habit, isCompleted) },
            { habit -> showEditHabitDialog(habit) },
            { habit -> showDeleteHabitDialog(habit) },
            { habit -> showReminderDialog(habit) }
        )
        habitsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        habitsRecyclerView.adapter = habitsAdapter
        
        // Don't call updateHabits here - it will be called in loadHabits()
        android.util.Log.d("HabitsFragment", "RecyclerView setup completed")
    }

    private fun setupFab() {
        addHabitFab.setOnClickListener {
            showAddHabitDialog()
        }
    }


    private fun showAddHabitDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_habit, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.habitNameInput)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.habitDescriptionInput)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.habitCategorySpinner)
        val prioritySpinner = dialogView.findViewById<Spinner>(R.id.habitPrioritySpinner)

        // Setup category spinner
        val categories = arrayOf("Health", "Fitness", "Mindfulness", "Learning", "Social", "Other")
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter

        // Setup priority spinner
        val priorities = arrayOf("High", "Medium", "Low")
        val priorityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, priorities)
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        prioritySpinner.adapter = priorityAdapter
        prioritySpinner.setSelection(1) // Default to Medium

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add New Habit")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = nameInput.text.toString().trim()
                val description = descriptionInput.text.toString().trim()
                val category = categorySpinner.selectedItem.toString()
                val priority = prioritySpinner.selectedItem.toString()

                if (name.isNotEmpty()) {
                    val newHabit = Habit(
                        id = System.currentTimeMillis().toString(),
                        name = name,
                        description = description,
                        category = category,
                        isCompleted = false,
                        createdAt = Date(),
                        lastCompletedAt = null,
                        currentStreak = 0,
                        longestStreak = 0,
                        totalCompletions = 0,
                        priority = priority,
                        todayStatus = "not_done",
                        reminderEnabled = false,
                        reminderTime = null,
                        reminderDays = emptyList(),
                        reminderMessage = ""
                    )
                    
                    // Add to list
                    habitsList.add(newHabit)
                    android.util.Log.d("HabitsFragment", "Added habit to list: ${newHabit.name} (${habitsList.size} total)")
                    
                    // Update static data immediately
                    setStaticHabitsList(habitsList.toMutableList())
                    setStaticDataLoaded(true)
                    
                    // Set flag to prevent reloading
                    justAddedHabit = true
                    
                    // Debug before save
                    android.util.Log.d("HabitsFragment", "Before save:")
                    debugAllData()
                    
                    // Save immediately
                    saveHabits()
                    
                    // Debug storage after save
                    debugStorage()
                    
                    // Update UI
                    if (::habitsAdapter.isInitialized) {
                        android.util.Log.d("HabitsFragment", "Calling notifyItemInserted")
                        habitsAdapter.notifyItemInserted(habitsList.size - 1)
                    } else {
                        android.util.Log.d("HabitsFragment", "Adapter not initialized, cannot notify")
                    }
                    updateProgress()
                    
                    
                    // Debug after UI update
                    android.util.Log.d("HabitsFragment", "After UI update:")
                    debugAllData()
                    
                    // Show success message
                    Toast.makeText(requireContext(), "Habit added successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Please enter a habit name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun showReminderDialog(habit: Habit) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_habit_reminder, null)
        val reminderToggle = dialogView.findViewById<Switch>(R.id.reminderToggleSwitch)
        val timeSelectionLayout = dialogView.findViewById<LinearLayout>(R.id.timeSelectionLayout)
        val daysSelectionLayout = dialogView.findViewById<LinearLayout>(R.id.daysSelectionLayout)
        val messageInputLayout = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.messageInputLayout)
        val timePickerButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.timePickerButton)
        val messageInput = dialogView.findViewById<EditText>(R.id.reminderMessageInput)
        
        // Day chips
        val dayChips = listOf(
            dialogView.findViewById<com.google.android.material.chip.Chip>(R.id.mondayChip),
            dialogView.findViewById<com.google.android.material.chip.Chip>(R.id.tuesdayChip),
            dialogView.findViewById<com.google.android.material.chip.Chip>(R.id.wednesdayChip),
            dialogView.findViewById<com.google.android.material.chip.Chip>(R.id.thursdayChip),
            dialogView.findViewById<com.google.android.material.chip.Chip>(R.id.fridayChip),
            dialogView.findViewById<com.google.android.material.chip.Chip>(R.id.saturdayChip),
            dialogView.findViewById<com.google.android.material.chip.Chip>(R.id.sundayChip)
        )
        
        val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        
        // Set current values
        reminderToggle.isChecked = habit.reminderEnabled
        if (habit.reminderTime != null) {
            timePickerButton.text = habit.reminderTime
        }
        messageInput.setText(habit.reminderMessage)
        
        // Set selected days
        habit.reminderDays.forEach { day ->
            val index = dayNames.indexOf(day)
            if (index >= 0) {
                dayChips[index].isChecked = true
            }
        }
        
        // Toggle visibility based on switch
        fun updateVisibility() {
            val isEnabled = reminderToggle.isChecked
            timeSelectionLayout.visibility = if (isEnabled) View.VISIBLE else View.GONE
            daysSelectionLayout.visibility = if (isEnabled) View.VISIBLE else View.GONE
            messageInputLayout.visibility = if (isEnabled) View.VISIBLE else View.GONE
        }
        
        reminderToggle.setOnCheckedChangeListener { _, _ -> updateVisibility() }
        updateVisibility()
        
        // Time picker
        timePickerButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            if (habit.reminderTime != null) {
                try {
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val time = timeFormat.parse(habit.reminderTime!!)
                    if (time != null) {
                        calendar.time = time
                    }
                } catch (e: Exception) {
                    // Use current time if parsing fails
                }
            }
            
            val timePickerDialog = TimePickerDialog(
                requireContext(),
                { _: TimePicker, hourOfDay: Int, minute: Int ->
                    val timeString = String.format("%02d:%02d", hourOfDay, minute)
                    timePickerButton.text = timeString
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            )
            timePickerDialog.show()
        }
        
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Set Reminder for ${habit.name}")
            .setView(dialogView)
            .setNeutralButton("Test (1 min)") { _, _ ->
                // Test reminder in 1 minute
                testReminder(habit)
            }
            .setPositiveButton("Save") { _, _ ->
                val isEnabled = reminderToggle.isChecked
                val selectedTime = if (isEnabled) timePickerButton.text.toString() else null
                val selectedDays = if (isEnabled) {
                    dayChips.mapIndexedNotNull { index, chip ->
                        if (chip.isChecked) dayNames[index] else null
                    }
                } else emptyList()
                val customMessage = if (isEnabled) messageInput.text.toString().trim() else ""
                
                // Update habit
                habit.reminderEnabled = isEnabled
                habit.reminderTime = selectedTime
                habit.reminderDays = selectedDays
                habit.reminderMessage = customMessage
                
                // Save and update UI
                saveHabits()
                habitsAdapter.notifyDataSetChanged()
                
                // Schedule notifications if enabled
                if (isEnabled && selectedTime != null && selectedDays.isNotEmpty()) {
                    scheduleHabitReminders(habit)
                    
                    // Show confirmation message
                    val message = "Reminder set for ${habit.name} at $selectedTime on ${selectedDays.joinToString(", ")}"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                } else if (isEnabled) {
                    Toast.makeText(requireContext(), "Please select a time and at least one day", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun calculateCurrentStreak(habit: Habit): Int {
        val today = Calendar.getInstance()
        var streak = 0
        var checkDate = Calendar.getInstance()
        
        // Check backwards from today
        for (i in 0..30) { // Check up to 30 days back
            val dateString = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(checkDate.time)
            val progressKey = "habits_progress_$dateString"
            val progressJson = sharedPreferences.getString(progressKey, "{}")
            val type = object : TypeToken<Map<String, Boolean>>() {}.type
            val progress = gson.fromJson<Map<String, Boolean>>(progressJson, type) ?: emptyMap()
            
            if (progress[habit.id] == true) {
                streak++
                checkDate.add(Calendar.DAY_OF_MONTH, -1)
            } else {
                break
            }
        }
        
        return streak
    }
    
    private fun scheduleHabitReminders(habit: Habit) {
        try {
            android.util.Log.d("HabitReminder", "Scheduling reminder for ${habit.name} at ${habit.reminderTime} on ${habit.reminderDays.joinToString()}")
            
            if (habit.reminderTime == null || habit.reminderDays.isEmpty()) {
                android.util.Log.w("HabitReminder", "Cannot schedule reminder: missing time or days")
                return
            }
            
            // First, cancel any existing reminders for this habit
            cancelExistingReminders(habit)
            
            // Parse the reminder time
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val reminderTime = timeFormat.parse(habit.reminderTime!!)
            if (reminderTime == null) {
                android.util.Log.w("HabitReminder", "Cannot parse reminder time: ${habit.reminderTime}")
                return
            }
            
            // Schedule reminders for each selected day
            habit.reminderDays.forEach { dayName ->
                scheduleReminderForDay(habit, dayName, reminderTime)
            }
            
            android.util.Log.d("HabitReminder", "Successfully scheduled ${habit.reminderDays.size} reminders for ${habit.name}")
            
        } catch (e: Exception) {
            android.util.Log.e("HabitReminder", "Error scheduling reminders for ${habit.name}: ${e.message}", e)
        }
    }
    
    private fun cancelExistingReminders(habit: Habit) {
        try {
            val workManager = androidx.work.WorkManager.getInstance(requireContext())
            
            // Cancel all existing reminders for this habit
            val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
            dayNames.forEach { dayName ->
                val tag = "habit_reminder_${habit.id}_$dayName"
                workManager.cancelAllWorkByTag(tag)
                android.util.Log.d("HabitReminder", "Cancelled existing reminders for ${habit.name} on $dayName")
            }
            
        } catch (e: Exception) {
            android.util.Log.e("HabitReminder", "Error cancelling existing reminders for ${habit.name}: ${e.message}", e)
        }
    }
    
    private fun scheduleReminderForDay(habit: Habit, dayName: String, reminderTime: Date) {
        try {
            val workManager = androidx.work.WorkManager.getInstance(requireContext())
            
            // Calculate the next occurrence of this day and time
            val calendar = Calendar.getInstance()
            val dayOfWeek = getDayOfWeek(dayName)
            val reminderCalendar = Calendar.getInstance()
            reminderCalendar.time = reminderTime
            val hour = reminderCalendar.get(Calendar.HOUR_OF_DAY)
            val minute = reminderCalendar.get(Calendar.MINUTE)
            
            // Set the time
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            // Find the next occurrence of this day
            while (calendar.get(Calendar.DAY_OF_WEEK) != dayOfWeek || calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            
            val delay = calendar.timeInMillis - System.currentTimeMillis()
            val hoursUntilReminder = delay / (1000 * 60 * 60)
            val minutesUntilReminder = (delay % (1000 * 60 * 60)) / (1000 * 60)
            
            android.util.Log.d("HabitReminder", "Scheduling reminder for ${habit.name} on $dayName at ${habit.reminderTime}")
            android.util.Log.d("HabitReminder", "Current time: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}")
            android.util.Log.d("HabitReminder", "Reminder time: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(calendar.time)}")
            android.util.Log.d("HabitReminder", "Delay: ${hoursUntilReminder}h ${minutesUntilReminder}m (${delay}ms)")
            
            if (delay <= 0) {
                android.util.Log.w("HabitReminder", "Delay is negative or zero! This might be why reminders aren't working.")
                return
            }
            
            // Create work request
            val workRequest = androidx.work.OneTimeWorkRequestBuilder<HabitReminderWorker>()
                .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
                .setInputData(
                    androidx.work.Data.Builder()
                        .putString("habit_id", habit.id)
                        .putString("habit_name", habit.name)
                        .putString("custom_message", habit.reminderMessage)
                        .build()
                )
                .addTag("habit_reminder_${habit.id}_$dayName")
                .build()
            
            // Enqueue the work
            workManager.enqueue(workRequest)
            
        } catch (e: Exception) {
            android.util.Log.e("HabitReminder", "Error scheduling reminder for $dayName: ${e.message}", e)
        }
    }
    
    private fun getDayOfWeek(dayName: String): Int {
        return when (dayName.lowercase()) {
            "monday" -> Calendar.MONDAY
            "tuesday" -> Calendar.TUESDAY
            "wednesday" -> Calendar.WEDNESDAY
            "thursday" -> Calendar.THURSDAY
            "friday" -> Calendar.FRIDAY
            "saturday" -> Calendar.SATURDAY
            "sunday" -> Calendar.SUNDAY
            else -> Calendar.MONDAY
        }
    }
    
    private fun testReminder(habit: Habit) {
        try {
            android.util.Log.d("HabitReminder", "Testing reminder for ${habit.name} in 1 minute")
            
            val workManager = androidx.work.WorkManager.getInstance(requireContext())
            
            // Create work request for 1 minute from now
            val workRequest = androidx.work.OneTimeWorkRequestBuilder<HabitReminderWorker>()
                .setInitialDelay(1, java.util.concurrent.TimeUnit.MINUTES)
                .setInputData(
                    androidx.work.Data.Builder()
                        .putString("habit_id", habit.id)
                        .putString("habit_name", habit.name)
                        .putString("custom_message", "Test reminder for ${habit.name}")
                        .build()
                )
                .addTag("test_reminder_${habit.id}")
                .build()
            
            // Enqueue the work
            workManager.enqueue(workRequest)
            
            Toast.makeText(requireContext(), "Test reminder scheduled for ${habit.name} in 1 minute!", Toast.LENGTH_LONG).show()
            android.util.Log.d("HabitReminder", "Test reminder scheduled successfully")
            
        } catch (e: Exception) {
            android.util.Log.e("HabitReminder", "Error scheduling test reminder: ${e.message}", e)
            Toast.makeText(requireContext(), "Error scheduling test reminder: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateHabitProgress(habit: Habit, isCompleted: Boolean) {
        val wasCompleted = habit.isCompleted
        habit.isCompleted = isCompleted
        
        // Update completion tracking
        if (isCompleted && !wasCompleted) {
            // Marking as completed
            habit.lastCompletedAt = Date()
            habit.totalCompletions++
            updateHabitStreak(habit, true)
        } else if (!isCompleted && wasCompleted) {
            // Marking as not completed
            updateHabitStreak(habit, false)
        }
        
        saveHabitProgress(habit.id, isCompleted)
        
        // Recalculate status and streaks for all habits after any change
        habitsList.forEach { h ->
            calculateHabitStatus(h)
            // Update streak when habit is completed
            if (isCompleted) {
                h.currentStreak = calculateCurrentStreak(h)
                if (h.currentStreak > h.longestStreak) {
                    h.longestStreak = h.currentStreak
                }
            }
        }
        
        saveHabits() // Save the updated habit data
        
        // Update progress immediately
        updateProgress()
        
        
        // Check achievements when habits are completed
        if (isCompleted) {
            // Achievement checking removed
        }
    }
    
    private fun updateHabitStreak(habit: Habit, completed: Boolean) {
        if (completed) {
            // Calculate current streak based on actual completion history
            val currentStreak = calculateCurrentStreak(habit)
            habit.currentStreak = currentStreak
            
            // Update longest streak
            if (habit.currentStreak > habit.longestStreak) {
                habit.longestStreak = habit.currentStreak
            }
        } else {
            // Recalculate streak when uncompleted
            habit.currentStreak = calculateCurrentStreak(habit)
        }
    }
    
    private fun migrateHabitToNewFormat(habit: Habit): Habit {
        return try {
            // Always create a new habit with all properties to ensure consistency
            val migratedHabit = Habit(
                id = habit.id,
                name = habit.name,
                description = habit.description,
                category = habit.category,
                isCompleted = habit.isCompleted,
                createdAt = habit.createdAt,
                lastCompletedAt = habit.lastCompletedAt ?: null,
                currentStreak = habit.currentStreak,
                longestStreak = habit.longestStreak,
                totalCompletions = habit.totalCompletions,
                priority = habit.priority ?: "Medium",
                todayStatus = "not_done",
                reminderEnabled = habit.reminderEnabled,
                reminderTime = habit.reminderTime,
                reminderDays = habit.reminderDays,
                reminderMessage = habit.reminderMessage
            )
            
            // Calculate status for the migrated habit
            calculateHabitStatus(migratedHabit)
            migratedHabit
        } catch (e: Exception) {
            // If migration fails, create a new habit with default values
            android.util.Log.e("HabitsFragment", "Error migrating habit: ${e.message}", e)
            Habit(
                id = habit.id,
                name = habit.name,
                description = habit.description,
                category = habit.category,
                isCompleted = habit.isCompleted,
                createdAt = habit.createdAt,
                lastCompletedAt = null,
                currentStreak = 0,
                longestStreak = 0,
                totalCompletions = 0,
                priority = "Medium",
                todayStatus = "not_done",
                reminderEnabled = false,
                reminderTime = null,
                reminderDays = emptyList(),
                reminderMessage = ""
            )
        }
    }
    
    private fun calculateHabitStatus(habit: Habit) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        // Check today's status
        val todayProgressKey = "habits_progress_${today.replace("-", "")}"
        val todayProgressJson = sharedPreferences.getString(todayProgressKey, "{}")
        val type = object : TypeToken<Map<String, Boolean>>() {}.type
        val todayProgress = gson.fromJson<Map<String, Boolean>>(todayProgressJson, type) ?: emptyMap()
        
        // Set today's status
        habit.todayStatus = when {
            todayProgress[habit.id] == true -> "done"
            else -> "not_done"
        }
        
        // Update isCompleted based on today's status
        habit.isCompleted = habit.todayStatus == "done"
    }

    private fun saveHabitProgress(habitId: String, isCompleted: Boolean) {
        val progressKey = "habits_progress_$today"
        val progressJson = sharedPreferences.getString(progressKey, "{}")
        val type = object : TypeToken<Map<String, Boolean>>() {}.type
        val progress = gson.fromJson<Map<String, Boolean>>(progressJson, type)?.toMutableMap() ?: mutableMapOf()
        
        progress[habitId] = isCompleted
        val updatedProgressJson = gson.toJson(progress)
        sharedPreferences.edit().putString(progressKey, updatedProgressJson).apply()
    }

    private fun updateProgress() {
        val completedCount = habitsList.count { it.isCompleted }
        val totalCount = habitsList.size
        
        if (totalCount > 0) {
            val progressPercentageValue = (completedCount * 100) / totalCount
            progressBar.progress = progressPercentageValue
            progressText.text = "$completedCount/$totalCount habits completed ($progressPercentageValue%)"
            progressPercentage.text = "$progressPercentageValue%"
        } else {
            progressBar.progress = 0
            progressText.text = "🌱 Start your wellness journey! Tap the + button to add your first habit."
            progressPercentage.text = "0%"
        }
    }

    private fun showEditHabitDialog(habit: Habit) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_habit, null)
        
        val nameInput = dialogView.findViewById<EditText>(R.id.habitNameInput)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.habitDescriptionInput)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.habitCategorySpinner)
        
        // Pre-fill with existing data
        nameInput.setText(habit.name)
        descriptionInput.setText(habit.description)
        
        // Set up category spinner
        val categories = arrayOf("Health", "Fitness", "Mindfulness", "Learning", "Work", "Personal", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
        categorySpinner.setSelection(categories.indexOf(habit.category))
        
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Habit")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val newName = nameInput.text.toString().trim()
                val newDescription = descriptionInput.text.toString().trim()
                val newCategory = categorySpinner.selectedItem.toString()
                
                if (newName.isNotEmpty()) {
                    val habitIndex = habitsList.indexOfFirst { it.id == habit.id }
                    if (habitIndex != -1) {
                        habitsList[habitIndex] = habit.copy(
                            name = newName,
                            description = newDescription,
                            category = newCategory
                        )
                        saveHabits()
                        habitsAdapter.notifyItemChanged(habitIndex)
                        updateProgress()
                        Toast.makeText(requireContext(), "Habit updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Please enter a habit name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteHabitDialog(habit: Habit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete '${habit.name}'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                val habitIndex = habitsList.indexOfFirst { it.id == habit.id }
                if (habitIndex != -1) {
                    habitsList.removeAt(habitIndex)
                    saveHabits()
                    habitsAdapter.notifyItemRemoved(habitIndex)
                    updateProgress()
                    Toast.makeText(requireContext(), "Habit deleted successfully!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    data class Habit(
        val id: String,
        val name: String,
        val description: String,
        val category: String,
        var isCompleted: Boolean,
        val createdAt: Date,
        var lastCompletedAt: Date? = null,
        var currentStreak: Int = 0,
        var longestStreak: Int = 0,
        var totalCompletions: Int = 0,
        var priority: String = "Medium", // High, Medium, Low
        var todayStatus: String = "not_done", // not_done, done
        var reminderEnabled: Boolean = false,
        var reminderTime: String? = null, // "HH:mm" format
        var reminderDays: List<String> = emptyList(), // ["Monday", "Tuesday", etc.]
        var reminderMessage: String = ""
    )

    class HabitsAdapter(
        private var habits: MutableList<Habit>,
        private val onHabitToggle: (Habit, Boolean) -> Unit,
        private val onEditHabit: (Habit) -> Unit,
        private val onDeleteHabit: (Habit) -> Unit,
        private val onSetReminder: (Habit) -> Unit
    ) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {

        class HabitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val nameText: TextView = view.findViewById(R.id.habitNameText)
            val descriptionText: TextView = view.findViewById(R.id.habitDescriptionText)
            val categoryText: TextView = view.findViewById(R.id.habitCategoryText)
            val priorityText: TextView = view.findViewById(R.id.habitPriorityText)
            val streakText: TextView = view.findViewById(R.id.streakText)
            val completionRateText: TextView = view.findViewById(R.id.completionRateText)
            val createdAtText: TextView = view.findViewById(R.id.createdAtText)
            val statusText: TextView = view.findViewById(R.id.statusText)
            val reminderText: TextView = view.findViewById(R.id.reminderText)
            val completionIndicator: View = view.findViewById(R.id.completionIndicator)
            val completedCheckbox: CheckBox = view.findViewById(R.id.habitCompletedCheckbox)
            val reminderButton: ImageButton = view.findViewById(R.id.reminderHabitButton)
            val editButton: ImageButton = view.findViewById(R.id.editHabitButton)
            val deleteButton: ImageButton = view.findViewById(R.id.deleteHabitButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_habit, parent, false)
            return HabitViewHolder(view)
        }

        override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
            val habit = habits[position]
            
            // Basic info
            holder.nameText.text = habit.name
            holder.descriptionText.text = habit.description
            holder.categoryText.text = habit.category
            
            // Priority
            val priority = habit.priority ?: "Medium"
            holder.priorityText.text = priority
            when (priority) {
                "High" -> holder.priorityText.setBackgroundColor(android.graphics.Color.parseColor("#FF5722"))
                "Medium" -> holder.priorityText.setBackgroundColor(android.graphics.Color.parseColor("#FF9800"))
                "Low" -> holder.priorityText.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
                else -> holder.priorityText.setBackgroundColor(android.graphics.Color.parseColor("#FF9800"))
            }
            
            // Hide streak info - only show creation date/time
            holder.streakText.visibility = View.GONE
            
            // Hide completion rate - only show creation date/time
            holder.completionRateText.visibility = View.GONE
            
            // Reminder info
            if (habit.reminderEnabled && habit.reminderTime != null) {
                holder.reminderText.visibility = View.VISIBLE
                holder.reminderText.text = "🔔 ${habit.reminderTime}"
            } else {
                holder.reminderText.visibility = View.GONE
            }
            
            // Creation date and time
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            holder.createdAtText.text = "Created: ${dateFormat.format(habit.createdAt)} at ${timeFormat.format(habit.createdAt)}"
            
            // Status indicator and text based on completion history
            when (habit.todayStatus) {
                "done" -> {
                    holder.completionIndicator.visibility = View.VISIBLE
                    holder.completionIndicator.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50")) // Green
                    holder.statusText.text = "✅ Done Today"
                    holder.statusText.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                    holder.statusText.visibility = View.VISIBLE
                }
                "not_done" -> {
                    holder.completionIndicator.visibility = View.GONE
                    holder.statusText.visibility = View.GONE
                }
            }
            
            // Remove listener before setting checked state to prevent infinite loop
            holder.completedCheckbox.setOnCheckedChangeListener(null)
            holder.completedCheckbox.isChecked = habit.isCompleted
            holder.completedCheckbox.setOnCheckedChangeListener { _, isChecked ->
                onHabitToggle(habit, isChecked)
            }

            holder.reminderButton.setOnClickListener {
                onSetReminder(habit)
            }

            holder.editButton.setOnClickListener {
                onEditHabit(habit)
            }

            holder.deleteButton.setOnClickListener {
                onDeleteHabit(habit)
            }
        }

        override fun getItemCount() = habits.size
        
        fun updateHabits(newHabits: MutableList<Habit>) {
            try {
                android.util.Log.d("HabitsFragment", "Adapter.updateHabits() called with ${newHabits.size} habits")
                android.util.Log.d("HabitsFragment", "Current adapter habits: ${habits.size}")
                
                // Use the same list reference instead of copying
                habits = newHabits
                android.util.Log.d("HabitsFragment", "Updated adapter to use same list reference: ${habits.size} habits")
                
                notifyDataSetChanged()
                android.util.Log.d("HabitsFragment", "notifyDataSetChanged() called")
            } catch (e: Exception) {
                android.util.Log.e("HabitsFragment", "Error in adapter.updateHabits: ${e.message}", e)
                // Fallback to safe state
                habits = mutableListOf()
            }
        }
    }

    private fun setupGlowEffects() {
        // Add twinkling effect to the FAB
        GlowAnimationUtils.createTwinkleEffect(addHabitFab, 4000L)
        
        // Add subtle pulse to progress section
        view?.let { rootView ->
            GlowAnimationUtils.createPulseEffect(rootView.findViewById(R.id.progressSection), 3000L)
        }
    }
    
    private fun debugStorage() {
        try {
            val habitsJson = sharedPreferences.getString("habits_list", "[]")
            val type = object : TypeToken<List<Habit>>() {}.type
            val storedHabits = gson.fromJson<List<Habit>>(habitsJson, type) ?: emptyList()
            android.util.Log.d("HabitsFragment", "DEBUG - Stored habits: ${storedHabits.size}")
            storedHabits.forEach { habit ->
                android.util.Log.d("HabitsFragment", "DEBUG - Habit: ${habit.name} (${habit.id})")
            }
        } catch (e: Exception) {
            android.util.Log.e("HabitsFragment", "DEBUG - Error reading storage: ${e.message}")
        }
    }
    
    private fun debugAllData() {
        android.util.Log.d("HabitsFragment", "=== DEBUG ALL DATA ===")
        android.util.Log.d("HabitsFragment", "habitsList.size: ${habitsList.size}")
        android.util.Log.d("HabitsFragment", "staticHabitsList.size: ${getStaticHabitsList().size}")
        android.util.Log.d("HabitsFragment", "isDataLoaded: $isDataLoaded")
        android.util.Log.d("HabitsFragment", "staticIsDataLoaded: ${isStaticDataLoaded()}")
        android.util.Log.d("HabitsFragment", "justAddedHabit: $justAddedHabit")
        android.util.Log.d("HabitsFragment", "Adapter initialized: ${::habitsAdapter.isInitialized}")
        if (::habitsAdapter.isInitialized) {
            android.util.Log.d("HabitsFragment", "Adapter habits.size: ${habitsAdapter.itemCount}")
        }
        android.util.Log.d("HabitsFragment", "=== END DEBUG ===")
    }
}
