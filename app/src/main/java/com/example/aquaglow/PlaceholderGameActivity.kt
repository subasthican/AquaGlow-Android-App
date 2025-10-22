package com.example.aquaglow

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

/**
 * Placeholder activity for games that are not yet implemented
 */
class PlaceholderGameActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_placeholder_game)
        
        val gameId = intent.getStringExtra("game_id") ?: "unknown"
        val gameTitle = intent.getStringExtra("game_title") ?: "Game"
        
        val titleText = findViewById<TextView>(R.id.gameTitleText)
        val descriptionText = findViewById<TextView>(R.id.gameDescriptionText)
        val backButton = findViewById<MaterialButton>(R.id.backButton)
        
        titleText.text = gameTitle
        descriptionText.text = "This game is coming soon! We're working hard to bring you an amazing gaming experience."
        
        backButton.setOnClickListener {
            finish()
        }
    }
}


