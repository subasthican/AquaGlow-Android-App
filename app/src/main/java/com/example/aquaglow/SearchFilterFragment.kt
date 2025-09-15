package com.example.aquaglow

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aquaglow.AuthActivity.Companion.PREFS
import com.example.aquaglow.databinding.FragmentSearchFilterBinding
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class SearchResult {
    data class Habit(val item: HabitItem) : SearchResult()
    data class Mood(val entry: MoodEntry) : SearchResult()
}

class SearchFilterFragment : Fragment() {

    private var _binding: FragmentSearchFilterBinding? = null
    private val binding get() = _binding!!

    private val gson = Gson()
    private val allHabits: MutableList<HabitItem> = mutableListOf()
    private val allMoods: MutableList<MoodEntry> = mutableListOf()
    private val results: MutableList<SearchResult> = mutableListOf()
    private lateinit var adapter: SearchResultsAdapter

    private var showHabits = true
    private var query: String = ""
    private var filterCompleted: Boolean? = null
    private var filterEmoji: String? = null
    private var dateFrom: Long? = null
    private var dateTo: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = SearchResultsAdapter(results)
        binding.recyclerResults.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerResults.adapter = adapter

        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.title = getString(R.string.search_filter)
        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        binding.searchField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                query = s?.toString()?.trim().orEmpty()
                applyFilters()
            }
        })

        binding.chipHabits.setOnClickListener { showHabits = true; applyFilters() }
        binding.chipMoods.setOnClickListener { showHabits = false; applyFilters() }

        binding.chipCompleted.setOnClickListener { filterCompleted = true; applyFilters() }
        binding.chipPending.setOnClickListener { filterCompleted = false; applyFilters() }
        binding.chipAllStatus.setOnClickListener { filterCompleted = null; applyFilters() }

        val emojis = listOf("😀","🙂","😐","😕","😢","😡","🥱","😴","🤒","🤗")
        emojis.forEach { e ->
            val chip = layoutInflater.inflate(R.layout.item_filter_chip, binding.chipGroupEmoji, false) as Chip
            chip.text = e
            chip.setOnClickListener { filterEmoji = e; applyFilters() }
            binding.chipGroupEmoji.addView(chip)
        }

        binding.buttonDateRange.setOnClickListener {
            // Minimal: toggle last 7 days; could be replaced with MaterialDatePicker
            val now = System.currentTimeMillis()
            dateFrom = now - 7 * 24 * 60 * 60 * 1000
            dateTo = now
            applyFilters()
        }

        binding.buttonClearDate.setOnClickListener {
            dateFrom = null; dateTo = null; applyFilters()
        }

        loadData()
        applyFilters()
    }

    private fun loadData() {
        allHabits.clear(); allMoods.clear()
        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val habitsJson = prefs.getString("habitsJson", null)
        if (!habitsJson.isNullOrEmpty()) {
            val type = object : TypeToken<List<HabitItem>>() {}.type
            allHabits.addAll(gson.fromJson(habitsJson, type))
        }
        val moodsJson = prefs.getString("moodsJson", null)
        if (!moodsJson.isNullOrEmpty()) {
            val type = object : TypeToken<List<MoodEntry>>() {}.type
            allMoods.addAll(gson.fromJson(moodsJson, type))
        }
    }

    private fun applyFilters() {
        results.clear()
        if (showHabits) {
            var list = allHabits.asSequence()
            if (query.isNotEmpty()) list = list.filter { it.name.contains(query, ignoreCase = true) }
            filterCompleted?.let { done -> list = list.filter { it.done == done } }
            results.addAll(list.map { SearchResult.Habit(it) }.toList())
        } else {
            var list = allMoods.asSequence()
            if (query.isNotEmpty()) list = list.filter { (it.note ?: "").contains(query, ignoreCase = true) }
            filterEmoji?.let { e -> list = list.filter { it.emoji == e } }
            val from = dateFrom; val to = dateTo
            if (from != null && to != null) list = list.filter { it.timestamp in from..to }
            results.addAll(list.map { SearchResult.Mood(it) }.toList())
        }
        binding.textEmpty.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
        adapter.notifyDataSetChanged()
    }

    companion object {
        fun formatDateTime(ts: Long): String = SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault()).format(Date(ts))
    }
}


