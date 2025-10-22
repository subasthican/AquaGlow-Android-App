package com.example.aquaglow

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * MoodFragment allows users to log moods with emojis and view mood history
 */
class MoodFragment : Fragment() {

    private lateinit var moodRecyclerView: RecyclerView
    private lateinit var moodNoteInput: EditText
    private lateinit var saveMoodButton: MaterialButton
    private lateinit var shareButton: FloatingActionButton
    private lateinit var selectDateButton: MaterialButton
    private lateinit var selectTimeButton: MaterialButton
    private lateinit var selectedDateTimeText: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var gson: Gson

    private var moodEntries = mutableListOf<MoodEntry>()
    private var selectedMood = 0 // 0-4 for different moods
    private var selectedDate: Date = Date() // Default to current date/time
    private var isCustomDateTime = false // Track if user selected custom date/time
    private var editingMoodId: String? = null // Track which mood is being edited

    private val moodEmojis = arrayOf("😢", "😐", "😊", "😄", "🤩")
    private val moodNames = arrayOf("Sad", "Neutral", "Happy", "Excited", "Amazing")
    private val moodScores = arrayOf(1, 2, 3, 4, 5)
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mood, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupSharedPreferences()
        loadMoodEntries()
        setupMoodSelector(view)
        setupRecyclerView()
        setupButtons()
        setupGlowEffects(view)
    }

    private fun initializeViews(view: View) {
        moodRecyclerView = view.findViewById(R.id.moodRecyclerView)
        moodNoteInput = view.findViewById(R.id.moodNoteInput)
        saveMoodButton = view.findViewById(R.id.saveMoodButton)
        shareButton = view.findViewById(R.id.shareButton)
        selectDateButton = view.findViewById(R.id.selectDateButton)
        selectTimeButton = view.findViewById(R.id.selectTimeButton)
        selectedDateTimeText = view.findViewById(R.id.selectedDateTimeText)
    }

    private fun setupSharedPreferences() {
        sharedPreferences = requireContext().getSharedPreferences("aquaglow_prefs", 0)
        gson = Gson()
    }

    private fun loadMoodEntries() {
        val moodJson = sharedPreferences.getString("mood_entries", "[]")
        val type = object : TypeToken<List<MoodEntry>>() {}.type
        moodEntries = gson.fromJson(moodJson, type) ?: mutableListOf()
    }

    private fun saveMoodEntries() {
        val moodJson = gson.toJson(moodEntries)
        sharedPreferences.edit().putString("mood_entries", moodJson).apply()
    }

    private fun setupMoodSelector(view: View) {
        val moodSelectorLayout = view.findViewById<LinearLayout>(R.id.moodSelectorLayout)
        
        // Create first row (3 buttons)
        val firstRow = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8
            }
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
        }
        
        // Create second row (2 buttons)
        val secondRow = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
        }
        
        // Add buttons to rows
        moodEmojis.forEachIndexed { index, emoji ->
            val moodButton = createMoodButton(emoji, moodNames[index], index)
            
            if (index < 3) {
                // First 3 buttons go in first row
                firstRow.addView(moodButton)
            } else {
                // Last 2 buttons go in second row
                secondRow.addView(moodButton)
            }
        }
        
        // Add rows to main layout
        moodSelectorLayout.addView(firstRow)
        moodSelectorLayout.addView(secondRow)
    }

    private fun createMoodButton(emoji: String, name: String, index: Int): com.google.android.material.button.MaterialButton {
        val button = com.google.android.material.button.MaterialButton(requireContext()).apply {
            text = "$emoji $name"
            textSize = 12f
            textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
            setPadding(8, 12, 8, 12)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(4, 4, 4, 4)
            }
            setOnClickListener {
                selectMood(index)
            }
            // Set minimum width to prevent too narrow buttons
            minimumWidth = 80
            // Prevent text wrapping
            maxLines = 1
            isSingleLine = true
            ellipsize = android.text.TextUtils.TruncateAt.END
        }
        return button
    }

    private fun selectMood(index: Int) {
        selectedMood = index
        // Update UI to show selected mood
        updateMoodSelection()
    }

    private fun updateMoodSelection() {
        val moodSelectorLayout = view?.findViewById<LinearLayout>(R.id.moodSelectorLayout)
        moodSelectorLayout?.let { layout ->
            var buttonIndex = 0
            // Iterate through rows
            for (rowIndex in 0 until layout.childCount) {
                val row = layout.getChildAt(rowIndex)
                if (row is LinearLayout) {
                    // Iterate through buttons in this row
                    for (buttonIndexInRow in 0 until row.childCount) {
                        val child = row.getChildAt(buttonIndexInRow)
                        if (child is com.google.android.material.button.MaterialButton) {
                            if (buttonIndex == selectedMood) {
                                // Selected mood styling
                                child.backgroundTintList = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.secondary))
                                child.setTextColor(requireContext().getColor(R.color.white))
                                child.strokeWidth = 0
                                child.alpha = 1.0f
                            } else {
                                // Unselected mood styling
                                child.backgroundTintList = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.surface_light))
                                child.setTextColor(requireContext().getColor(R.color.text_primary_light))
                                child.strokeWidth = 1
                                child.strokeColor = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.primary))
                                child.alpha = 0.7f
                            }
                            buttonIndex++
                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        val adapter = MoodHistoryAdapter(moodEntries, 
            onShareClick = { moodEntry ->
                shareMood(moodEntry)
            },
            onDeleteClick = { moodEntry ->
                deleteMood(moodEntry)
            },
            onEditClick = { moodEntry ->
                editMood(moodEntry)
            }
        )
        moodRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        moodRecyclerView.adapter = adapter
    }

    private fun setupButtons() {
        saveMoodButton.setOnClickListener {
            saveMood()
        }

        shareButton.setOnClickListener {
            if (moodEntries.isNotEmpty()) {
                shareMood(moodEntries.first())
            }
        }
        
        selectDateButton.setOnClickListener {
            showDatePicker()
        }
        
        selectTimeButton.setOnClickListener {
            showTimePicker()
        }
        
        // Initialize date/time display
        updateDateTimeDisplay()
    }

    private fun saveMood() {
        val note = moodNoteInput.text.toString().trim()
        
        if (editingMoodId != null) {
            // Editing existing mood
            val index = moodEntries.indexOfFirst { it.id == editingMoodId }
            if (index != -1) {
                val updatedMoodEntry = MoodEntry(
                    id = editingMoodId!!, // Keep the same ID
                    moodEmoji = moodEmojis[selectedMood],
                    moodName = moodNames[selectedMood],
                    moodScore = moodScores[selectedMood],
                    note = note,
                    timestamp = selectedDate
                )
                
                moodEntries[index] = updatedMoodEntry
                moodRecyclerView.adapter?.notifyItemChanged(index)
                
                Toast.makeText(requireContext(), "Mood updated!", Toast.LENGTH_SHORT).show()
            }
            editingMoodId = null // Clear editing state
        } else {
            // Adding new mood - always use current timestamp for new entries
            val moodEntry = MoodEntry(
                id = System.currentTimeMillis().toString(),
                moodEmoji = moodEmojis[selectedMood],
                moodName = moodNames[selectedMood],
                moodScore = moodScores[selectedMood],
                note = note,
                timestamp = Date() // Always use current time for new entries
            )

            moodEntries.add(0, moodEntry) // Add to beginning for newest first
            moodRecyclerView.adapter?.notifyItemInserted(0)
            
            // Check achievements when mood is logged
            // Achievement checking removed
            
            Toast.makeText(requireContext(), getString(R.string.mood_saved), Toast.LENGTH_SHORT).show()
        }
        
        saveMoodEntries()
        
        // Clear input
        moodNoteInput.text.clear()
        
        // Reset to current date/time for next entry
        selectedDate = Date()
        isCustomDateTime = false
        updateDateTimeDisplay()
        
        // Reset save button text
        saveMoodButton.text = "Save Mood"
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
        
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val newCalendar = Calendar.getInstance()
                newCalendar.set(year, month, dayOfMonth)
                // Preserve the time part
                val timeCalendar = Calendar.getInstance()
                timeCalendar.time = selectedDate
                newCalendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                newCalendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                
                selectedDate = newCalendar.time
                isCustomDateTime = true
                updateDateTimeDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        // Don't allow future dates
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
    
    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
        
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val newCalendar = Calendar.getInstance()
                newCalendar.time = selectedDate
                newCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                newCalendar.set(Calendar.MINUTE, minute)
                
                selectedDate = newCalendar.time
                isCustomDateTime = true
                updateDateTimeDisplay()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true // 24-hour format
        )
        
        timePickerDialog.show()
    }
    
    private fun updateDateTimeDisplay() {
        if (isCustomDateTime) {
            selectedDateTimeText.text = "Selected: ${dateTimeFormat.format(selectedDate)}"
            selectedDateTimeText.setTextColor(requireContext().getColor(R.color.secondary))
        } else {
            selectedDateTimeText.text = "Using current date and time"
            selectedDateTimeText.setTextColor(requireContext().getColor(R.color.text_secondary_light))
        }
    }

    private fun shareMood(moodEntry: MoodEntry) {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        val shareText = "My mood on ${dateFormat.format(moodEntry.timestamp)} was ${moodEntry.moodEmoji} - ${moodEntry.moodName}${if (moodEntry.note.isNotEmpty()) "\nNote: ${moodEntry.note}" else ""}"
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share your mood"))
    }
    
    private fun deleteMood(moodEntry: MoodEntry) {
        val index = moodEntries.indexOfFirst { it.id == moodEntry.id }
        if (index != -1) {
            moodEntries.removeAt(index)
            saveMoodEntries()
            moodRecyclerView.adapter?.notifyItemRemoved(index)
            Toast.makeText(requireContext(), "Mood deleted", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun editMood(moodEntry: MoodEntry) {
        // Set the selected mood and note for editing
        val moodIndex = moodEmojis.indexOf(moodEntry.moodEmoji)
        if (moodIndex != -1) {
            selectedMood = moodIndex
            updateMoodSelection()
        }
        
        moodNoteInput.setText(moodEntry.note)
        
        // Set the date/time for editing
        selectedDate = moodEntry.timestamp
        isCustomDateTime = true
        updateDateTimeDisplay()
        
        // Store the ID of the entry being edited (don't remove it yet)
        editingMoodId = moodEntry.id
        
        // Update save button text to show editing mode
        saveMoodButton.text = "Update Mood"
        
        Toast.makeText(requireContext(), "Mood loaded for editing", Toast.LENGTH_SHORT).show()
    }

    data class MoodEntry(
        val id: String,
        val moodEmoji: String,
        val moodName: String,
        val moodScore: Int,
        val note: String,
        val timestamp: Date
    )

    class MoodHistoryAdapter(
        private val moodEntries: List<MoodEntry>,
        private val onShareClick: (MoodEntry) -> Unit,
        private val onDeleteClick: (MoodEntry) -> Unit,
        private val onEditClick: (MoodEntry) -> Unit
    ) : RecyclerView.Adapter<MoodHistoryAdapter.MoodViewHolder>() {

        class MoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val moodEmoji: TextView = view.findViewById(R.id.moodEmoji)
            val moodName: TextView = view.findViewById(R.id.moodName)
            val moodNote: TextView = view.findViewById(R.id.moodNote)
            val moodTime: TextView = view.findViewById(R.id.moodTime)
            val shareButton: ImageButton = view.findViewById(R.id.shareMoodButton)
            val editButton: ImageButton = view.findViewById(R.id.editMoodButton)
            val deleteButton: ImageButton = view.findViewById(R.id.deleteMoodButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_mood_entry, parent, false)
            return MoodViewHolder(view)
        }

        override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
            val moodEntry = moodEntries[position]
            
            holder.moodEmoji.text = moodEntry.moodEmoji
            holder.moodName.text = moodEntry.moodName
            holder.moodNote.text = moodEntry.note
            holder.moodTime.text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(moodEntry.timestamp)

            holder.shareButton.setOnClickListener {
                onShareClick(moodEntry)
            }
            
            holder.editButton.setOnClickListener {
                onEditClick(moodEntry)
            }
            
            holder.deleteButton.setOnClickListener {
                onDeleteClick(moodEntry)
            }
        }

        override fun getItemCount() = moodEntries.size
    }

    private fun setupGlowEffects(view: View) {
        // Add glow movement to save button
        GlowAnimationUtils.createBreathingEffect(saveMoodButton, 4000L)
        
        // Add breathing effect to share button
        GlowAnimationUtils.createBreathingEffect(shareButton, 4000L)
        
        // Add pulse effect to mood selector
        // val moodSelector = view.findViewById<View>(R.id.moodSelector)
        // GlowAnimationUtils.createPulseEffect(moodSelector, 3000L)
        
        // Apply material glow effects to buttons
        GlowAnimationUtils.applyMaterialGlow(saveMoodButton)
        // Note: shareButton is FloatingActionButton, not MaterialButton
    }
}