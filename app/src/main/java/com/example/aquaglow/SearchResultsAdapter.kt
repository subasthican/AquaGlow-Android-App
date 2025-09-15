package com.example.aquaglow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SearchResultsAdapter(private val items: List<SearchResult>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_HABIT = 1
    private val TYPE_MOOD = 2

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is SearchResult.Habit -> TYPE_HABIT
        is SearchResult.Mood -> TYPE_MOOD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HABIT) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_result_habit, parent, false)
            HabitVH(v)
        } else {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_result_mood, parent, false)
            MoodVH(v)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is SearchResult.Habit -> (holder as HabitVH).bind(item.item)
            is SearchResult.Mood -> (holder as MoodVH).bind(item.entry)
        }
    }

    class HabitVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.textName)
        private val status: TextView = itemView.findViewById(R.id.textStatus)
        fun bind(item: HabitItem) {
            name.text = item.name
            status.text = if (item.done) itemView.context.getString(R.string.completed) else itemView.context.getString(R.string.pending)
        }
    }

    class MoodVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val emoji: TextView = itemView.findViewById(R.id.textEmoji)
        private val note: TextView = itemView.findViewById(R.id.textNote)
        private val time: TextView = itemView.findViewById(R.id.textTime)
        fun bind(entry: MoodEntry) {
            emoji.text = entry.emoji
            note.text = entry.note ?: ""
            time.text = SearchFilterFragment.formatDateTime(entry.timestamp)
        }
    }
}


