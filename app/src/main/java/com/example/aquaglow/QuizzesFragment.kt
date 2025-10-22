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
 * QuizzesFragment displays wellness quizzes and interactive content
 */
class QuizzesFragment : Fragment() {

    private lateinit var quizzesRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var completedQuizzesText: TextView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var refreshButton: MaterialButton

    private lateinit var quizzesAdapter: QuizzesAdapter
    private var quizzesList = mutableListOf<QuizManager.Quiz>()
    private var completedQuizzes = 0
    private var loadingIndicator: ProgressBar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quizzes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackButton(view)

        initializeViews(view)
        setupRecyclerView()
        setupClickListeners()
        showLoading()
        loadQuizzes()
        updateProgress()
        hideLoading()
        setupGlowEffects(view)
    }
    
    private fun showLoading() {
        loadingIndicator?.visibility = View.VISIBLE
        quizzesRecyclerView.visibility = View.GONE
    }
    
    private fun hideLoading() {
        loadingIndicator?.visibility = View.GONE
        quizzesRecyclerView.visibility = View.VISIBLE
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this fragment
        loadQuizzes()
        updateProgress()
    }

    private fun setupBackButton(view: View) {
        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initializeViews(view: View) {
        quizzesRecyclerView = view.findViewById(R.id.quizzesRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        progressText = view.findViewById(R.id.progressText)
        completedQuizzesText = view.findViewById(R.id.completedQuizzesText)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        refreshButton = view.findViewById(R.id.refreshButton)
    }

    private fun setupRecyclerView() {
        quizzesAdapter = QuizzesAdapter(quizzesList) { quiz ->
            startQuiz(quiz)
        }
        quizzesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = quizzesAdapter
        }
    }

    private fun setupClickListeners() {
        refreshButton.setOnClickListener {
            showLoading()
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                loadQuizzes()
                hideLoading()
            }, 500)
        }
    }

    private fun loadQuizzes() {
        val allQuizzes = QuizManager.getAllQuizzes(requireContext())
        quizzesList.clear()
        quizzesList.addAll(allQuizzes)
        
        completedQuizzes = QuizManager.getCompletedQuizzesCount(requireContext())
        
        quizzesAdapter.notifyDataSetChanged()
        updateProgress()
        updateEmptyState()
    }

    private fun updateProgress() {
        val totalQuizzes = quizzesList.size
        val progress = if (totalQuizzes > 0) (completedQuizzes * 100) / totalQuizzes else 0
        
        progressBar.progress = progress
        progressText.text = "$completedQuizzes/$totalQuizzes completed"
        completedQuizzesText.text = "Completed: $completedQuizzes quizzes"
    }

    private fun updateEmptyState() {
        if (quizzesList.isEmpty()) {
            emptyStateLayout.visibility = View.VISIBLE
            quizzesRecyclerView.visibility = View.GONE
        } else {
            emptyStateLayout.visibility = View.GONE
            quizzesRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun startQuiz(quiz: QuizManager.Quiz) {
        // Launch QuizActivity
        val intent = android.content.Intent(requireContext(), QuizActivity::class.java)
        intent.putExtra("quiz_id", quiz.id)
        startActivity(intent)
    }

    private fun setupGlowEffects(view: View) {
        // Apply glow effects to buttons
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
 * Adapter for quizzes RecyclerView
 */
class QuizzesAdapter(
    private val quizzes: List<QuizManager.Quiz>,
    private val onQuizClick: (QuizManager.Quiz) -> Unit
) : RecyclerView.Adapter<QuizzesAdapter.QuizViewHolder>() {

    class QuizViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.quizTitleText)
        val descriptionText: TextView = itemView.findViewById(R.id.quizDescriptionText)
        val categoryText: TextView = itemView.findViewById(R.id.quizCategoryText)
        val difficultyText: TextView = itemView.findViewById(R.id.quizDifficultyText)
        val timeText: TextView = itemView.findViewById(R.id.quizTimeText)
        val pointsText: TextView = itemView.findViewById(R.id.quizPointsText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quiz, parent, false)
        return QuizViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val quiz = quizzes[position]
        
        holder.titleText.text = quiz.title
        holder.descriptionText.text = quiz.description
        holder.categoryText.text = quiz.category.name.replace("_", " ")
        holder.difficultyText.text = quiz.difficulty.name
        holder.timeText.text = "${quiz.estimatedTime} min"
        holder.pointsText.text = "${quiz.questions.size} questions"
        
        holder.itemView.setOnClickListener {
            onQuizClick(quiz)
        }
    }

    override fun getItemCount(): Int = quizzes.size
}




