package com.example.aquaglow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(private val items: List<Long>) : RecyclerView.Adapter<HistoryAdapter.VH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_notification_history, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val time: TextView = itemView.findViewById(R.id.textTime)
        fun bind(ts: Long) {
            time.text = SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault()).format(Date(ts))
        }
    }
}


