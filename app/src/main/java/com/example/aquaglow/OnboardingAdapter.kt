package com.example.aquaglow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * OnboardingAdapter provides the pages for the onboarding ViewPager2
 * Each page contains a title, description, and illustration
 */
class OnboardingAdapter(private val activity: OnboardingActivity) : 
    RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {
    
    private val onboardingPages = listOf(
        OnboardingPage(
            title = R.string.onboarding_title_1,
            description = R.string.onboarding_desc_1,
            icon = R.drawable.ic_mood_track
        ),
        OnboardingPage(
            title = R.string.onboarding_title_2,
            description = R.string.onboarding_desc_2,
            icon = R.drawable.ic_habits
        ),
        OnboardingPage(
            title = R.string.onboarding_title_3,
            description = R.string.onboarding_desc_3,
            icon = R.drawable.ic_hydration
        )
    )
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding_page, parent, false)
        return OnboardingViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(onboardingPages[position])
    }
    
    override fun getItemCount(): Int = onboardingPages.size
    
    /**
     * ViewHolder for onboarding pages
     */
    class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconImageView: ImageView = itemView.findViewById(R.id.iconImageView)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        
        fun bind(page: OnboardingPage) {
            iconImageView.setImageResource(page.icon)
            titleTextView.text = itemView.context.getString(page.title)
            descriptionTextView.text = itemView.context.getString(page.description)
        }
    }
    
    /**
     * Data class representing an onboarding page
     */
    data class OnboardingPage(
        val title: Int,
        val description: Int,
        val icon: Int
    )
}

