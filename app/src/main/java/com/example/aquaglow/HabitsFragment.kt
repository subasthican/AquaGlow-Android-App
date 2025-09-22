package com.example.aquaglow

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
    private lateinit var addHabitFab: FloatingActionButton
    private lateinit var habitsAdapter: HabitsAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var gson: Gson

    private var habitsList = mutableListOf<Habit>()
    private val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_habits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupSharedPreferences()
        loadHabits()
        setupRecyclerView()
        setupFab()
        updateProgress()
    }

    private fun initializeViews(view: View) {
        habitsRecyclerView = view.findViewById(R.id.habitsRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        progressText = view.findViewById(R.id.progressText)
        addHabitFab = view.findViewById(R.id.addHabitFab)
    }

    private fun setupSharedPreferences() {
        sharedPreferences = requireContext().getSharedPreferences("aquaglow_prefs", 0)
        gson = Gson()
    }

    private fun loadHabits() {
        val habitsJson = sharedPreferences.getString("habits_list", "[]")
        val type = object : TypeToken<List<Habit>>() {}.type
        habitsList = gson.fromJson(habitsJson, type) ?: mutableListOf()
    }

    private fun saveHabits() {
        val habitsJson = gson.toJson(habitsList)
        sharedPreferences.edit().putString("habits_list", habitsJson).apply()
    }

    private fun setupRecyclerView() {
        habitsAdapter = HabitsAdapter(habitsList) { habit, isCompleted ->
            updateHabitProgress(habit, isCompleted)
        }
        habitsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        habitsRecyclerView.adapter = habitsAdapter
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

        // Setup category spinner
        val categories = arrayOf("Health", "Fitness", "Mindfulness", "Learning", "Social", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add New Habit")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = nameInput.text.toString().trim()
                val description = descriptionInput.text.toString().trim()
                val category = categorySpinner.selectedItem.toString()

                if (name.isNotEmpty()) {
                    val newHabit = Habit(
                        id = System.currentTimeMillis().toString(),
                        name = name,
                        description = description,
                        category = category,
                        isCompleted = false,
                        createdAt = Date()
                    )
                    habitsList.add(newHabit)
                    saveHabits()
                    habitsAdapter.notifyItemInserted(habitsList.size - 1)
                    updateProgress()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun updateHabitProgress(habit: Habit, isCompleted: Boolean) {
        habit.isCompleted = isCompleted
        saveHabitProgress(habit.id, isCompleted)
        updateProgress()
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
            val progressPercentage = (completedCount * 100) / totalCount
            progressBar.progress = progressPercentage
            progressText.text = "$completedCount/$totalCount habits completed ($progressPercentage%)"
        } else {
            progressBar.progress = 0
            progressText.text = "No habits yet. Add your first habit!"
        }
    }

    data class Habit(
        val id: String,
        val name: String,
        val description: String,
        val category: String,
        var isCompleted: Boolean,
        val createdAt: Date
    )

    class HabitsAdapter(
        private val habits: List<Habit>,
        private val onHabitToggle: (Habit, Boolean) -> Unit
    ) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {

        class HabitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val nameText: TextView = view.findViewById(R.id.habitNameText)
            val descriptionText: TextView = view.findViewById(R.id.habitDescriptionText)
            val categoryText: TextView = view.findViewById(R.id.habitCategoryText)
            val completedCheckbox: CheckBox = view.findViewById(R.id.habitCompletedCheckbox)
            val deleteButton: ImageButton = view.findViewById(R.id.deleteHabitButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_habit, parent, false)
            return HabitViewHolder(view)
        }

        override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
            val habit = habits[position]
            
            holder.nameText.text = habit.name
            holder.descriptionText.text = habit.description
            holder.categoryText.text = habit.category
            holder.completedCheckbox.isChecked = habit.isCompleted

            holder.completedCheckbox.setOnCheckedChangeListener { _, isChecked ->
                onHabitToggle(habit, isChecked)
            }

            holder.deleteButton.setOnClickListener {
                // TODO: Implement delete functionality
            }
        }

        override fun getItemCount() = habits.size
    }
}