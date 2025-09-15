package com.example.aquaglow

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aquaglow.AuthActivity.Companion.PREFS
import com.example.aquaglow.databinding.FragmentHabitsBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class HabitItem(var id: Long, var name: String, var done: Boolean)

class HabitTrackerFragment : Fragment(), HabitViewHolder.HabitRowListener {

    private var _binding: FragmentHabitsBinding? = null
    private val binding get() = _binding!!

    private val gson = Gson()
    private val habits: MutableList<HabitItem> = mutableListOf()
    private lateinit var adapter: HabitsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = HabitsAdapter(habits, this)
        binding.recyclerHabits.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerHabits.adapter = adapter

        binding.fabAddHabit.setOnClickListener { showAddDialog() }

        loadFromPrefs()
        updateProgress()
    }

    override fun onResume() {
        super.onResume()
        loadFromPrefs()
        updateProgress()
    }

    private fun showAddDialog(existing: HabitItem? = null) {
        val input = EditText(requireContext())
        input.hint = getString(R.string.habit_name_hint)
        if (existing != null) input.setText(existing.name)
        AlertDialog.Builder(requireContext())
            .setTitle(if (existing == null) R.string.add_habit else R.string.edit_habit)
            .setView(input)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    if (existing == null) {
                        habits.add(HabitItem(System.currentTimeMillis(), name, false))
                    } else {
                        existing.name = name
                    }
                    saveToPrefs()
                    adapter.notifyDataSetChanged()
                    updateProgress()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun loadFromPrefs() {
        habits.clear()
        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString("habitsJson", null)
        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<HabitItem>>() {}.type
            habits.addAll(gson.fromJson(json, type))
        }
        adapter.notifyDataSetChanged()
    }

    private fun saveToPrefs() {
        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString("habitsJson", gson.toJson(habits)).apply()
        // also update aggregated progress for Home
        val percent = if (habits.isEmpty()) 0 else (habits.count { it.done } * 100 / habits.size)
        prefs.edit().putInt("habitProgressToday", percent).apply()
    }

    private fun updateProgress() {
        val percent = if (habits.isEmpty()) 0 else (habits.count { it.done } * 100 / habits.size)
        binding.progressBar.progress = percent
        binding.textProgress.text = getString(R.string.habits_progress_format, percent)
    }

    // HabitRowListener
    override fun onToggleDone(item: HabitItem, done: Boolean) {
        item.done = done
        saveToPrefs()
        updateProgress()
    }

    override fun onEdit(item: HabitItem) {
        showAddDialog(item)
    }

    override fun onDelete(item: HabitItem) {
        habits.removeAll { it.id == item.id }
        saveToPrefs()
        adapter.notifyDataSetChanged()
        updateProgress()
    }
}


