package com.example.aquaglow

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

/**
 * GoalsFragment displays user goals and weekly progress
 */
class GoalsFragment : Fragment() {

    private lateinit var goalsRecyclerView: RecyclerView
    private lateinit var weeklyProgressBar: ProgressBar
    private lateinit var weeklyProgressText: TextView
    private lateinit var weeklyProgressDescription: TextView
    private lateinit var addGoalButton: com.google.android.material.button.MaterialButton
    private lateinit var emptyStateLayout: LinearLayout

    private lateinit var goalsAdapter: GoalsAdapter
    private var goalsList = mutableListOf<GoalManager.Goal>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_goals, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackButton(view)
        initializeViews(view)
        setupRecyclerView()
        setupClickListeners()
        loadGoals()
        updateWeeklyProgress()
        setupGlowEffects(view)
    }

    private fun setupBackButton(view: View) {
        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initializeViews(view: View) {
        goalsRecyclerView = view.findViewById(R.id.goalsRecyclerView)
        weeklyProgressBar = view.findViewById(R.id.weeklyProgressBar)
        weeklyProgressText = view.findViewById(R.id.weeklyProgressText)
        weeklyProgressDescription = view.findViewById(R.id.weeklyProgressDescription)
        addGoalButton = view.findViewById(R.id.addGoalButton)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
    }

    private fun setupRecyclerView() {
        goalsAdapter = GoalsAdapter(goalsList) { goal ->
            // Handle goal click - could show details or edit
        }
        goalsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = goalsAdapter
        }
    }

    private fun setupClickListeners() {
        addGoalButton.setOnClickListener {
            showAddGoalDialog()
        }
    }

    private fun loadGoals() {
        goalsList.clear()
        goalsList.addAll(GoalManager.getAllGoals(requireContext()))
        goalsAdapter.notifyDataSetChanged()
        
        // Show/hide empty state
        emptyStateLayout.visibility = if (goalsList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateWeeklyProgress() {
        val progress = GoalManager.getWeeklyGoalProgress(requireContext())
        weeklyProgressBar.progress = progress
        weeklyProgressText.text = "$progress%"
        
        val description = when {
            progress >= 90 -> "Outstanding! You're crushing your goals! 🏆"
            progress >= 70 -> "Great job! You're on track! ⭐"
            progress >= 50 -> "Good progress! Keep it up! 👍"
            progress >= 30 -> "You're getting there! 💪"
            else -> "Start working towards your goals today! 🌱"
        }
        weeklyProgressDescription.text = description
    }

    private fun showAddGoalDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_goal, null)
        
        val titleInput = dialogView.findViewById<EditText>(R.id.goalTitleInput)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.goalDescriptionInput)
        val targetInput = dialogView.findViewById<EditText>(R.id.goalTargetInput)
        val unitInput = dialogView.findViewById<EditText>(R.id.goalUnitInput)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.goalCategorySpinner)

        // Setup category spinner
        val categories = arrayOf("Health", "Fitness", "Mindfulness", "Learning", "Social", "Work", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add New Goal")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = titleInput.text.toString().trim()
                val description = descriptionInput.text.toString().trim()
                val target = targetInput.text.toString().trim().toIntOrNull() ?: 0
                val unit = unitInput.text.toString().trim()
                val category = categorySpinner.selectedItem.toString()

                if (title.isNotEmpty() && target > 0) {
                    val newGoal = GoalManager.Goal(
                        id = System.currentTimeMillis().toString(),
                        title = title,
                        description = description,
                        targetValue = target,
                        unit = unit,
                        category = category
                    )
                    GoalManager.addGoal(requireContext(), newGoal)
                    loadGoals()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun setupGlowEffects(view: View) {
        // Add breathing effect to weekly progress bar
        GlowAnimationUtils.createBreathingEffect(weeklyProgressBar, 3000L)
        
        // Add pulse effect to add goal button
        GlowAnimationUtils.createPulseEffect(addGoalButton, 2000L)
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this fragment
        loadGoals()
        updateWeeklyProgress()
    }
}

/**
 * Adapter for displaying goals in RecyclerView
 */
class GoalsAdapter(
    private val goals: List<GoalManager.Goal>,
    private val onGoalClick: (GoalManager.Goal) -> Unit
) : RecyclerView.Adapter<GoalsAdapter.GoalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_goal, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        holder.bind(goals[position])
    }

    override fun getItemCount(): Int = goals.size

    class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.goalTitle)
        private val description: TextView = itemView.findViewById(R.id.goalDescription)
        private val progress: ProgressBar = itemView.findViewById(R.id.goalProgress)
        private val progressText: TextView = itemView.findViewById(R.id.goalProgressText)
        private val status: TextView = itemView.findViewById(R.id.goalStatus)
        private val category: TextView = itemView.findViewById(R.id.goalCategory)
        private val date: TextView = itemView.findViewById(R.id.goalDate)

        fun bind(goal: GoalManager.Goal) {
            title.text = goal.title
            description.text = goal.description
            category.text = goal.category
            
            val progressPercentage = if (goal.targetValue > 0) {
                (goal.currentValue.toFloat() / goal.targetValue * 100).toInt()
            } else 0
            
            progress.progress = progressPercentage.coerceAtMost(100)
            progressText.text = "${goal.currentValue}/${goal.targetValue} ${goal.unit}"
            
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            date.text = dateFormat.format(Date(goal.createdAt))
            
            if (goal.isCompleted) {
                status.text = "🏆"
                // Add glow effect to completed goals
                GlowAnimationUtils.createBreathingEffect(itemView, 4000L)
            } else {
                status.text = "🎯"
            }
            
            itemView.setOnClickListener {
                // Handle goal click - could show details or edit
            }
        }
    }
}
