package com.example.superheroi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class TiebreakerActivity : AppCompatActivity() {
    
    private lateinit var heroesLinearLayout: LinearLayout
    private var tiedHeroIds: List<String> = emptyList()
    
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
    
    companion object {
        private const val TAG = "TiebreakerActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tiebreaker)
        
        heroesLinearLayout = findViewById(R.id.heroesLinearLayout)
        
        // Get tied hero IDs from intent
        tiedHeroIds = intent.getStringArrayListExtra("TIED_HERO_IDS")?.toList() ?: emptyList()
        
        Log.d(TAG, "Tiebreaker screen opened with heroes: $tiedHeroIds")
        
        if (tiedHeroIds.isEmpty()) {
            // Fallback: go directly to result with ironman
            goToResult("ironman")
            return
        }
        
        // Create a card for each tied hero
        tiedHeroIds.forEach { heroId ->
            val hero = heroes[heroId]
            if (hero != null) {
                addHeroCard(hero)
            }
        }
    }
    
    private fun addHeroCard(hero: Hero) {
        // Create MaterialCardView
        val card = MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 24
            }
            radius = 16f
            cardElevation = 8f
            setContentPadding(24, 24, 24, 24)
        }
        
        // Create LinearLayout inside card
        val cardContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        
        // Hero Image
        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200)
            setImageResource(hero.imageRes)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        
        // Hero Name
        val nameTextView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16
            }
            text = getString(hero.nameRes)
            textSize = 24f
            setTextColor(getColor(R.color.accent))
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        
        // Hero Description
        val descTextView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16
            }
            text = getString(hero.descRes)
            textSize = 14f
            setTextColor(getColor(R.color.text_primary))
            gravity = Gravity.CENTER
        }
        
        // Choose Button
        val chooseButton = MaterialButton(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 24
            }
            text = getString(R.string.choose_hero)
            textSize = 16f
            isAllCaps = false
            setOnClickListener {
                Log.d(TAG, "User selected hero: ${hero.id}")
                goToResult(hero.id)
            }
        }
        
        // Add views to card
        cardContent.addView(imageView)
        cardContent.addView(nameTextView)
        cardContent.addView(descTextView)
        cardContent.addView(chooseButton)
        
        card.addView(cardContent)
        heroesLinearLayout.addView(card)
    }
    
    private fun goToResult(heroId: String) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("HERO_ID", heroId)
        startActivity(intent)
        finish()
    }
}
