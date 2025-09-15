package com.example.aquaglow

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aquaglow.AuthActivity.Companion.PREFS
import com.example.aquaglow.databinding.FragmentMoodBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MoodEntry(val emoji: String, val note: String?, val timestamp: Long)

class MoodJournalFragment : Fragment() {

    private var _binding: FragmentMoodBinding? = null
    private val binding get() = _binding!!
    private val gson = Gson()
    private val entries: MutableList<MoodEntry> = mutableListOf()
    private lateinit var adapter: MoodHistoryAdapter

    private val emojiOptions = listOf("😀","🙂","😐","😕","😢","😡","🥱","😴","🤒","🤗")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = MoodHistoryAdapter(entries)
        binding.recyclerHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerHistory.adapter = adapter

        // simple emoji grid by setting text on buttons
        val buttons = listOf(binding.emoji1,binding.emoji2,binding.emoji3,binding.emoji4,binding.emoji5,binding.emoji6,binding.emoji7,binding.emoji8,binding.emoji9,binding.emoji10)
        buttons.forEachIndexed { index, button ->
            button.text = emojiOptions[index]
            button.setOnClickListener {
                binding.textSelectedEmoji.text = emojiOptions[index]
            }
        }

        binding.buttonSaveMood.setOnClickListener { saveMood() }

        loadEntries()
    }

    private fun saveMood() {
        val emoji = binding.textSelectedEmoji.text?.toString().takeUnless { it.isNullOrEmpty() } ?: "🙂"
        val note = binding.inputNote.editText?.text?.toString()?.trim().takeUnless { it.isNullOrEmpty() }
        val entry = MoodEntry(emoji, note, System.currentTimeMillis())
        entries.add(0, entry)
        saveEntries()
        adapter.notifyDataSetChanged()

        // update last mood for Home
        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(entry.timestamp))
        prefs.edit().putString("lastMoodEmoji", emoji).putString("lastMoodTime", time).apply()
    }

    private fun loadEntries() {
        entries.clear()
        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString("moodsJson", null)
        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<MoodEntry>>() {}.type
            entries.addAll(gson.fromJson(json, type))
        }
        adapter.notifyDataSetChanged()
    }

    private fun saveEntries() {
        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString("moodsJson", gson.toJson(entries)).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


