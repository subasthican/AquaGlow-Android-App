package com.example.aquaglow

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dialog fragment showing daily wellness log for a selected date
 */
class DailyLogDialogFragment : DialogFragment() {

    private lateinit var dialogDateText: TextView
    private lateinit var habitsText: TextView
    private lateinit var habitsProgressBar: ProgressBar
    private lateinit var moodEmojiText: TextView
    private lateinit var moodNoteText: TextView
    private lateinit var waterIntakeText: TextView
    private lateinit var waterProgressBar: ProgressBar
    private lateinit var activityText: TextView
    private lateinit var activityCard: View
    private lateinit var closeButton: ImageButton
    private lateinit var closeDialogButton: MaterialButton

    private lateinit var selectedDate: Date
    private val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get selected date from arguments
        val dateInMillis = arguments?.getLong(ARG_DATE, System.currentTimeMillis()) ?: System.currentTimeMillis()
        selectedDate = Date(dateInMillis)
        
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_daily_log, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupCloseButtons()
        loadDailyLog()
    }

    private fun initializeViews(view: View) {
        dialogDateText = view.findViewById(R.id.dialogDateText)
        habitsText = view.findViewById(R.id.habitsText)
        habitsProgressBar = view.findViewById(R.id.habitsProgressBar)
        moodEmojiText = view.findViewById(R.id.moodEmojiText)
        moodNoteText = view.findViewById(R.id.moodNoteText)
        waterIntakeText = view.findViewById(R.id.waterIntakeText)
        waterProgressBar = view.findViewById(R.id.waterProgressBar)
        activityText = view.findViewById(R.id.activityText)
        activityCard = view.findViewById(R.id.activityCard)
        closeButton = view.findViewById(R.id.closeButton)
        closeDialogButton = view.findViewById(R.id.closeDialogButton)

        // Set date text
        dialogDateText.text = dateFormat.format(selectedDate)
    }

    private fun setupCloseButtons() {
        closeButton.setOnClickListener {
            dismiss()
        }

        closeDialogButton.setOnClickListener {
            dismiss()
        }
    }

    private fun loadDailyLog() {
        try {
            val log = CalendarDataManager.getDailyLog(requireContext(), selectedDate)

            // Load habits
            if (log.totalHabits > 0) {
                habitsText.text = "${log.habitsCompleted}/${log.totalHabits} ${getString(R.string.calendar_habits_completed)}"
                habitsProgressBar.progress = log.completionPercentage
            } else {
                habitsText.text = getString(R.string.calendar_no_habits)
                habitsProgressBar.progress = 0
            }

            // Load mood
            if (log.moodEmoji != null) {
                moodEmojiText.text = log.moodEmoji
                moodEmojiText.visibility = View.VISIBLE
                moodNoteText.text = log.moodNote ?: getString(R.string.calendar_no_notes)
            } else {
                moodEmojiText.visibility = View.GONE
                moodNoteText.text = getString(R.string.calendar_no_mood)
            }

            // Load water intake
            if (log.waterGlasses > 0) {
                waterIntakeText.text = "${log.waterGlasses}/${log.waterGoal} ${getString(R.string.calendar_glasses)}"
                val waterPercentage = (log.waterGlasses.toFloat() / log.waterGoal * 100).toInt().coerceAtMost(100)
                waterProgressBar.progress = waterPercentage
            } else {
                waterIntakeText.text = getString(R.string.calendar_no_water)
                waterProgressBar.progress = 0
            }

            // Load activity (steps & shakes) - only show for today
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val logDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate)
            
            if (today == logDate && (log.stepCount > 0 || log.shakeCount > 0)) {
                activityCard.visibility = View.VISIBLE
                activityText.text = "Steps: ${log.stepCount} | Shakes: ${log.shakeCount}"
            } else {
                activityCard.visibility = View.GONE
            }
        } catch (e: Exception) {
            android.util.Log.e("DailyLogDialogFragment", "Error loading daily log: ${e.message}", e)
            // Set default values on error
            habitsText.text = getString(R.string.calendar_no_habits)
            habitsProgressBar.progress = 0
            moodEmojiText.visibility = View.GONE
            moodNoteText.text = getString(R.string.calendar_no_mood)
            waterIntakeText.text = getString(R.string.calendar_no_water)
            waterProgressBar.progress = 0
            activityCard.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    companion object {
        private const val ARG_DATE = "date"

        fun newInstance(date: Date): DailyLogDialogFragment {
            val fragment = DailyLogDialogFragment()
            val args = Bundle()
            args.putLong(ARG_DATE, date.time)
            fragment.arguments = args
            return fragment
        }
    }
}


