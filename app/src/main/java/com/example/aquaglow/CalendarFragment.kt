package com.example.aquaglow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.fragment.app.Fragment
import java.util.*

/**
 * Calendar Fragment - Shows wellness calendar with daily logs and monthly summary
 */
class CalendarFragment : Fragment() {

    private lateinit var calendarView: CalendarView

    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupCalendar()
        setupGlowEffects(view)
    }

    private fun initializeViews(view: View) {
        calendarView = view.findViewById(R.id.calendarView)
    }

    private fun setupCalendar() {
        // Set calendar to today
        calendarView.date = calendar.timeInMillis

        // Handle date selection
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            val selectedDate = calendar.time
            
            showDailyLogDialog(selectedDate)
        }
    }

    private fun showDailyLogDialog(date: Date) {
        val dialog = DailyLogDialogFragment.newInstance(date)
        dialog.show(parentFragmentManager, "DailyLogDialog")
    }


    private fun setupGlowEffects(view: View) {
        // Calendar glow effects can be added here if needed
    }

    override fun onResume() {
        super.onResume()
        // Calendar data is automatically refreshed
    }

    companion object {
        fun newInstance() = CalendarFragment()
    }
}


