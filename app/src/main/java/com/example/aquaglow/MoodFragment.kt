package com.example.aquaglow

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
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var gson: Gson

    private var moodEntries = mutableListOf<MoodEntry>()
    private var selectedMood = 0 // 0-4 for different moods

    private val moodEmojis = arrayOf("😢", "😐", "😊", "😄", "🤩")
    private val moodNames = arrayOf("Sad", "Neutral", "Happy", "Excited", "Amazing")
    private val moodScores = arrayOf(1, 2, 3, 4, 5)

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
    }

    private fun initializeViews(view: View) {
        moodRecyclerView = view.findViewById(R.id.moodRecyclerView)
        moodNoteInput = view.findViewById(R.id.moodNoteInput)
        saveMoodButton = view.findViewById(R.id.saveMoodButton)
        shareButton = view.findViewById(R.id.shareButton)
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
        
        moodEmojis.forEachIndexed { index, emoji ->
            val moodButton = createMoodButton(emoji, moodNames[index], index)
            moodSelectorLayout.addView(moodButton)
        }
    }

    private fun createMoodButton(emoji: String, name: String, index: Int): Button {
        val button = Button(requireContext()).apply {
            text = "$emoji\n$name"
            textSize = 12f
            setPadding(16, 16, 16, 16)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener {
                selectMood(index)
            }
        }
        return button
    }

    private fun selectMood(index: Int) {
        selectedMood = index
        // Update UI to show selected mood
        updateMoodSelection()
    }

    private fun updateMoodSelection() {
        // TODO: Update visual feedback for selected mood
    }

    private fun setupRecyclerView() {
        val adapter = MoodHistoryAdapter(moodEntries) { moodEntry ->
            shareMood(moodEntry)
        }
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
    }

    private fun saveMood() {
        val note = moodNoteInput.text.toString().trim()
        val moodEntry = MoodEntry(
            id = System.currentTimeMillis().toString(),
            moodEmoji = moodEmojis[selectedMood],
            moodName = moodNames[selectedMood],
            moodScore = moodScores[selectedMood],
            note = note,
            timestamp = Date()
        )

        moodEntries.add(0, moodEntry) // Add to beginning for newest first
        saveMoodEntries()
        
        // Update RecyclerView
        moodRecyclerView.adapter?.notifyItemInserted(0)
        
        // Clear input
        moodNoteInput.text.clear()
        
        // Show success message
        Toast.makeText(requireContext(), "Mood saved! 😊", Toast.LENGTH_SHORT).show()
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
        private val onShareClick: (MoodEntry) -> Unit
    ) : RecyclerView.Adapter<MoodHistoryAdapter.MoodViewHolder>() {

        class MoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val moodEmoji: TextView = view.findViewById(R.id.moodEmoji)
            val moodName: TextView = view.findViewById(R.id.moodName)
            val moodNote: TextView = view.findViewById(R.id.moodNote)
            val moodTime: TextView = view.findViewById(R.id.moodTime)
            val shareButton: ImageButton = view.findViewById(R.id.shareMoodButton)
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
        }

        override fun getItemCount() = moodEntries.size
    }
}