package com.example.aquaglow

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator

/**
 * QuizActivity - Interactive quiz-taking interface
 */
class QuizActivity : AppCompatActivity() {

    private lateinit var quiz: QuizManager.Quiz
    private var currentQuestionIndex = 0
    private var score = 0
    private val userAnswers = mutableListOf<Int>()

    // UI Components
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var questionNumberText: TextView
    private lateinit var questionText: TextView
    private lateinit var optionsGroup: RadioGroup
    private lateinit var option1: RadioButton
    private lateinit var option2: RadioButton
    private lateinit var option3: RadioButton
    private lateinit var option4: RadioButton
    private lateinit var previousButton: MaterialButton
    private lateinit var nextButton: MaterialButton
    private lateinit var submitButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        // Get quiz ID from intent
        val quizId = intent.getStringExtra("quiz_id") ?: ""
        
        // Load quiz
        val loadedQuiz = QuizManager.getQuizById(this, quizId)
        if (loadedQuiz == null) {
            finish()
            return
        }
        quiz = loadedQuiz

        initializeViews()
        setupClickListeners()
        setupBackPressedHandler()
        displayQuestion()
    }
    
    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@QuizActivity)
                    .setTitle("Exit Quiz?")
                    .setMessage("Your progress will be lost. Are you sure?")
                    .setPositiveButton("Yes") { _, _ ->
                        finish()
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        })
    }

    private fun initializeViews() {
        progressBar = findViewById(R.id.quizProgressBar)
        questionNumberText = findViewById(R.id.questionNumberText)
        questionText = findViewById(R.id.questionText)
        optionsGroup = findViewById(R.id.optionsGroup)
        option1 = findViewById(R.id.option1)
        option2 = findViewById(R.id.option2)
        option3 = findViewById(R.id.option3)
        option4 = findViewById(R.id.option4)
        previousButton = findViewById(R.id.previousButton)
        nextButton = findViewById(R.id.nextButton)
        submitButton = findViewById(R.id.submitButton)

        // Set toolbar
        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
            title = quiz.title
        }
    }

    private fun setupClickListeners() {
        previousButton.setOnClickListener {
            if (currentQuestionIndex > 0) {
                saveCurrentAnswer()
                currentQuestionIndex--
                displayQuestion()
            }
        }

        nextButton.setOnClickListener {
            if (validateAnswer()) {
                saveCurrentAnswer()
                if (currentQuestionIndex < quiz.questions.size - 1) {
                    currentQuestionIndex++
                    displayQuestion()
                } else {
                    // Last question, show submit button
                    nextButton.visibility = View.GONE
                    submitButton.visibility = View.VISIBLE
                }
            } else {
                android.widget.Toast.makeText(this, "Please select an answer", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        submitButton.setOnClickListener {
            if (validateAnswer()) {
                saveCurrentAnswer()
                showResults()
            } else {
                android.widget.Toast.makeText(this, "Please answer all questions", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayQuestion() {
        val question = quiz.questions[currentQuestionIndex]
        
        // Update progress
        progressBar.progress = ((currentQuestionIndex + 1) * 100) / quiz.questions.size
        questionNumberText.text = "Question ${currentQuestionIndex + 1} of ${quiz.questions.size}"
        
        // Display question
        questionText.text = question.text
        
        // Clear previous selection
        optionsGroup.clearCheck()
        
        // Display options based on question type
        when (question.type) {
            QuizManager.QuestionType.MULTIPLE_CHOICE -> {
                displayMultipleChoice(question)
            }
            QuizManager.QuestionType.TRUE_FALSE -> {
                displayTrueFalse()
            }
            else -> {
                // For other types, show multiple choice
                displayMultipleChoice(question)
            }
        }
        
        // Restore previous answer if exists
        if (currentQuestionIndex < userAnswers.size) {
            val previousAnswer = userAnswers[currentQuestionIndex]
            when (previousAnswer) {
                0 -> option1.isChecked = true
                1 -> option2.isChecked = true
                2 -> option3.isChecked = true
                3 -> option4.isChecked = true
            }
        }
        
        // Update button visibility
        previousButton.isEnabled = currentQuestionIndex > 0
        
        if (currentQuestionIndex == quiz.questions.size - 1) {
            nextButton.visibility = View.GONE
            submitButton.visibility = View.VISIBLE
        } else {
            nextButton.visibility = View.VISIBLE
            submitButton.visibility = View.GONE
        }
    }

    private fun displayMultipleChoice(question: QuizManager.Question) {
        option1.visibility = View.VISIBLE
        option2.visibility = View.VISIBLE
        option3.visibility = View.VISIBLE
        option4.visibility = View.VISIBLE
        
        if (question.options.size >= 1) option1.text = question.options[0]
        if (question.options.size >= 2) option2.text = question.options[1]
        if (question.options.size >= 3) option3.text = question.options[2]
        if (question.options.size >= 4) option4.text = question.options[3]
        
        // Hide unused options
        if (question.options.size < 4) option4.visibility = View.GONE
        if (question.options.size < 3) option3.visibility = View.GONE
    }

    private fun displayTrueFalse() {
        option1.visibility = View.VISIBLE
        option2.visibility = View.VISIBLE
        option3.visibility = View.GONE
        option4.visibility = View.GONE
        
        option1.text = "True"
        option2.text = "False"
    }

    private fun validateAnswer(): Boolean {
        return optionsGroup.checkedRadioButtonId != -1
    }

    private fun saveCurrentAnswer() {
        val selectedId = optionsGroup.checkedRadioButtonId
        val selectedAnswer = when (selectedId) {
            R.id.option1 -> 0
            R.id.option2 -> 1
            R.id.option3 -> 2
            R.id.option4 -> 3
            else -> -1
        }
        
        if (selectedAnswer != -1) {
            if (currentQuestionIndex < userAnswers.size) {
                userAnswers[currentQuestionIndex] = selectedAnswer
            } else {
                userAnswers.add(selectedAnswer)
            }
        }
    }

    private fun calculateScore() {
        score = 0
        for (i in quiz.questions.indices) {
            val question = quiz.questions[i]
            if (i < userAnswers.size && userAnswers[i] == question.correctAnswer) {
                score += question.points
            }
        }
    }

    private fun showResults() {
        calculateScore()
        
        val totalPoints = quiz.questions.sumOf { it.points }
        val percentage = (score.toFloat() / totalPoints * 100).toInt()
        
        // Save result
        val result = QuizManager.QuizResult(
            quizId = quiz.id,
            score = score,
            totalQuestions = quiz.questions.size,
            percentage = percentage.toFloat(),
            completedAt = System.currentTimeMillis(),
            answers = userAnswers.toList(),
            insights = generateInsights(percentage)
        )
        QuizManager.saveQuizResult(this, result)
        
        // Send completion notification
        sendCompletionNotification(percentage)
        
        // Show results dialog
        val message = buildString {
            append("Your Score: $score/$totalPoints\n")
            append("Percentage: $percentage%\n\n")
            append(getPerformanceMessage(percentage))
            append("\n\n")
            append("Correct Answers: ${userAnswers.count { i -> quiz.questions.getOrNull(userAnswers.indexOf(i))?.correctAnswer == i }}")
        }
        
        AlertDialog.Builder(this)
            .setTitle("🎉 Quiz Complete!")
            .setMessage(message)
            .setPositiveButton("Review Answers") { _, _ ->
                // TODO: Show answer review
                finish()
            }
            .setNegativeButton("Finish") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun getPerformanceMessage(percentage: Int): String {
        return when {
            percentage >= 90 -> "🌟 Excellent! You're a wellness expert!"
            percentage >= 75 -> "👍 Great job! You know your stuff!"
            percentage >= 60 -> "✅ Good work! Keep learning!"
            percentage >= 40 -> "📚 Not bad! Review the material."
            else -> "💪 Keep trying! Practice makes perfect!"
        }
    }

    private fun generateInsights(percentage: Int): List<String> {
        return listOf(
            "You scored $percentage% on ${quiz.title}",
            getPerformanceMessage(percentage)
        )
    }
    
    private fun sendCompletionNotification(percentage: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        
        // Create notification channel
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "quiz_completion",
                "Quiz Completion",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for quiz completion"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // Create notification
        val notification = androidx.core.app.NotificationCompat.Builder(this, "quiz_completion")
            .setSmallIcon(R.drawable.ic_trending_up)
            .setContentTitle("🎉 Quiz Completed!")
            .setContentText("${quiz.title}: $percentage% - ${getPerformanceMessage(percentage)}")
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle()
                .bigText("Congratulations! You completed ${quiz.title}\n\nScore: $score points\nPercentage: $percentage%\n\n${getPerformanceMessage(percentage)}"))
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        
        notificationManager.notify(quiz.id.hashCode(), notification)
    }
}

