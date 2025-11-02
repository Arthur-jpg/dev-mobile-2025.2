package com.example.superheroi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class ResultActivity : AppCompatActivity() {
    
    private lateinit var heroImageView: ImageView
    private lateinit var heroNameTextView: TextView
    private lateinit var heroDescriptionTextView: TextView
    private lateinit var shareButton: MaterialButton
    private lateinit var githubButton: MaterialButton
    private lateinit var restartButton: MaterialButton
    
    private data class Hero(
        val id: String,
        val nameRes: Int,
        val descRes: Int,
        val imageRes: Int
    )
    
    private val heroes = mapOf(
        "ironman" to Hero("ironman", R.string.hero_ironman, R.string.hero_ironman_desc, R.drawable.hero_ironman),
        "hulk" to Hero("hulk", R.string.hero_hulk, R.string.hero_hulk_desc, R.drawable.hero_hulk),
        "captain" to Hero("captain", R.string.hero_captainamerica, R.string.hero_captainamerica_desc, R.drawable.hero_captain),
        "strange" to Hero("strange", R.string.hero_doctorstrange, R.string.hero_doctorstrange_desc, R.drawable.hero_strange),
        "thor" to Hero("thor", R.string.hero_thor, R.string.hero_thor_desc, R.drawable.hero_thor),
        "blackwidow" to Hero("blackwidow", R.string.hero_blackwidow, R.string.hero_blackwidow_desc, R.drawable.hero_blackwidow),
        "spiderman" to Hero("spiderman", R.string.hero_spiderman, R.string.hero_spiderman_desc, R.drawable.hero_spiderman)
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        
        // Initialize views
        heroImageView = findViewById(R.id.heroImageView)
        heroNameTextView = findViewById(R.id.heroNameTextView)
        heroDescriptionTextView = findViewById(R.id.heroDescriptionTextView)
        shareButton = findViewById(R.id.shareButton)
        githubButton = findViewById(R.id.githubButton)
        restartButton = findViewById(R.id.restartButton)
        
        // Get hero ID from intent
        val heroId = intent.getStringExtra("HERO_ID") ?: "ironman"
        val hero = heroes[heroId] ?: heroes["ironman"]!!
        
        // Display hero information
        displayHero(hero)
        
        // Set button click listeners
        shareButton.setOnClickListener {
            shareResult(hero)
        }
        
        githubButton.setOnClickListener {
            openGitHub()
        }
        
        restartButton.setOnClickListener {
            restartQuiz()
        }
    }
    
    private fun displayHero(hero: Hero) {
        heroImageView.setImageResource(hero.imageRes)
        heroNameTextView.text = getString(hero.nameRes)
        heroDescriptionTextView.text = getString(hero.descRes)
    }
    
    private fun shareResult(hero: Hero) {
        // Intent implícita para compartilhar resultado
        val heroName = getString(hero.nameRes)
        val shareText = getString(R.string.share_text, heroName)
        
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        intent.putExtra(Intent.EXTRA_TEXT, shareText)
        
        startActivity(Intent.createChooser(intent, getString(R.string.share_via)))
    }
    
    private fun openGitHub() {
        // Intent implícita para abrir link do GitHub
        val githubUrl = "https://github.com/Arthur-jpg"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
        startActivity(intent)
    }
    
    private fun restartQuiz() {
        // Return to MainActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
    
    override fun onBackPressed() {
        // Override back button to return to main activity
        restartQuiz()
    }
}
