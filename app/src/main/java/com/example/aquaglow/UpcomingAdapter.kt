package com.example.aquaglow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UpcomingAdapter(
    private val items: List<ReminderSchedule>,
    private val listener: Listener
) : RecyclerView.Adapter<UpcomingAdapter.VH>() {

    interface Listener { fun onToggleEnabled(enabled: Boolean) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_upcoming_reminder, parent, false)
        return VH(v, listener)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    class VH(itemView: View, private val listener: Listener) : RecyclerView.ViewHolder(itemView) {
        private val time: TextView = itemView.findViewById(R.id.textTime)
        private val interval: TextView = itemView.findViewById(R.id.textInterval)
        private val toggle: Switch = itemView.findViewById(R.id.switchEnabled)
        fun bind(item: ReminderSchedule) {
            val formatted = if (item.nextTimeMillis > 0) SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(item.nextTimeMillis)) else "—"
            time.text = formatted
            interval.text = itemView.context.getString(R.string.reminder_interval_hours, item.intervalHours)
            toggle.setOnCheckedChangeListener(null)
            toggle.isChecked = item.enabled
            toggle.setOnCheckedChangeListener { _, isChecked -> listener.onToggleEnabled(isChecked) }
        }
    }
}


