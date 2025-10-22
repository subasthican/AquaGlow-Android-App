package com.example.aquaglow

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

/**
 * HelpActivity provides user guide and FAQ for the AquaGlow app
 */
class HelpActivity : AppCompatActivity() {

    private lateinit var helpRecyclerView: RecyclerView
    private lateinit var helpAdapter: HelpAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_help)

            initializeViews()
            setupHelpContent()
            setupBackPressedCallback()
            
            try {
                setupGlowEffects()
            } catch (e: Exception) {
                android.util.Log.e("HelpActivity", "Failed to setup glow effects", e)
            }
        } catch (e: Exception) {
            android.util.Log.e("HelpActivity", "Failed to create HelpActivity", e)
            finish()
        }
    }

    /**
     * Initialize all UI views
     */
    private fun initializeViews() {
        helpRecyclerView = findViewById(R.id.helpRecyclerView)
        helpRecyclerView.layoutManager = LinearLayoutManager(this)

        // Toolbar back button
        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }
    }

    /**
     * Set up help content and FAQ
     */
    private fun setupHelpContent() {
        val helpItems = listOf(
            HelpItem(
                "Getting Started",
                "Welcome to AquaGlow! Start by adding your first habit in the Habits tab. You can track daily activities like drinking water, exercising, or meditating. Each habit can be marked as completed by tapping the checkbox.",
                R.drawable.ic_habits
            ),
            HelpItem(
                "Mood Tracking",
                "Log your daily mood using the emoji selector in the Mood tab. You can add optional notes to describe how you're feeling. Your mood history is saved and can be viewed anytime.",
                R.drawable.ic_mood
            ),
            HelpItem(
                "Hydration Reminders",
                "Set up hydration reminders in Settings to get notified when it's time to drink water. You can customize the interval from 1 to 6 hours based on your preference.",
                R.drawable.ic_water_drop
            ),
            HelpItem(
                "View Statistics",
                "Check your weekly mood trends and habit completion rates in the Statistics tab. The chart shows your mood patterns over the last 7 days.",
                R.drawable.ic_stats_nav
            ),
            HelpItem(
                "Customize Settings",
                "Personalize your experience in Settings. Change the app theme, adjust notification preferences, and manage your data.",
                R.drawable.ic_settings_nav
            ),
            HelpItem(
                "Data Privacy",
                "Your data is stored locally on your device using SharedPreferences. We don't collect or share your personal information. You can clear all data anytime from Settings.",
                R.drawable.ic_security
            ),
            HelpItem(
                "Troubleshooting",
                "If you experience issues, try restarting the app or clearing the app data. Make sure notifications are enabled for hydration reminders to work properly.",
                R.drawable.ic_bug_report
            ),
            HelpItem(
                "Tips for Success",
                "• Set realistic daily goals\n• Check in with your mood regularly\n• Use the app consistently for best results\n• Don't worry about perfect streaks - progress matters more",
                R.drawable.ic_lightbulb
            )
        )

        helpAdapter = HelpAdapter(helpItems)
        helpRecyclerView.adapter = helpAdapter
    }

    /**
     * Set up back pressed callback for modern Android
     */
    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun setupGlowEffects() {
        // Add breathing effect to help recycler view
        // GlowAnimationUtils.createBreathingEffect(helpRecyclerView, 4000L)
    }
}

/**
 * Data class for help items
 */
data class HelpItem(
    val title: String,
    val description: String,
    val iconRes: Int
)

/**
 * Adapter for help items RecyclerView
 */
class HelpAdapter(private val helpItems: List<HelpItem>) : 
    RecyclerView.Adapter<HelpAdapter.HelpViewHolder>() {

    class HelpViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.helpTitleTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.helpDescriptionTextView)
        val iconImageView: android.widget.ImageView = itemView.findViewById(R.id.helpIconImageView)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): HelpViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_help, parent, false)
        return HelpViewHolder(view)
    }

    override fun onBindViewHolder(holder: HelpViewHolder, position: Int) {
        val item = helpItems[position]
        holder.titleTextView.text = item.title
        holder.descriptionTextView.text = item.description
        holder.iconImageView.setImageResource(item.iconRes)
    }

    override fun getItemCount(): Int = helpItems.size
}