package com.example.aquaglow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox

class HabitsAdapter(
    private val items: List<HabitItem>,
    private val listener: HabitViewHolder.HabitRowListener
) : RecyclerView.Adapter<HabitViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view, listener)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(items[position])
    }
}

class HabitViewHolder(itemView: View, private val listener: HabitRowListener) : RecyclerView.ViewHolder(itemView) {

    private val check: MaterialCheckBox = itemView.findViewById(R.id.checkDone)
    private val name: TextView = itemView.findViewById(R.id.textName)
    private val edit: ImageButton = itemView.findViewById(R.id.buttonEdit)
    private val delete: ImageButton = itemView.findViewById(R.id.buttonDelete)

    fun bind(item: HabitItem) {
        name.text = item.name
        check.isChecked = item.done
        check.setOnCheckedChangeListener(null)
        check.setOnCheckedChangeListener { _, isChecked ->
            listener.onToggleDone(item, isChecked)
        }
        edit.setOnClickListener { listener.onEdit(item) }
        delete.setOnClickListener { listener.onDelete(item) }
    }

    interface HabitRowListener {
        fun onToggleDone(item: HabitItem, done: Boolean)
        fun onEdit(item: HabitItem)
        fun onDelete(item: HabitItem)
    }
}


