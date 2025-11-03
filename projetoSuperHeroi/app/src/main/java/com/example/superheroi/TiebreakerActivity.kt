package com.example.superheroi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TiebreakerActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tiebreaker)
        
        val heroesLinearLayout = findViewById<LinearLayout>(R.id.heroesLinearLayout)

        val tiedHeroIds = intent.getStringArrayListExtra("TIED_HERO_IDS")
        
        Log.d("TiebreakerActivity", "Tela de desempate com heróis: $tiedHeroIds")
        
        if (tiedHeroIds == null || tiedHeroIds.isEmpty()) {
            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra("HERO_ID", "ironman")
            startActivity(intent)
            finish()
            return
        }

        for (heroId in tiedHeroIds) {
            val cardLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(40, 40, 40, 40)
                setBackgroundResource(android.R.color.white)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 40
                }
            }

            val heroImage = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(300, 300)
                scaleType = ImageView.ScaleType.FIT_CENTER
            }

            val heroName = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 24
                }
                textSize = 24f
                gravity = Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val heroDesc = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 16
                }
                textSize = 14f
                gravity = Gravity.CENTER
            }

            val chooseButton = Button(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 32
                }
                text = "Escolher este herói"
                textSize = 16f
            }

            if (heroId == "ironman") {
                heroImage.setImageResource(R.drawable.hero_ironman)
                heroName.text = "Homem de Ferro"
                heroDesc.text = "Você é inovador, inteligente e estratégico."
            } else if (heroId == "hulk") {
                heroImage.setImageResource(R.drawable.hero_hulk)
                heroName.text = "Hulk"
                heroDesc.text = "Você é forte, poderoso e determinado."
            } else if (heroId == "captain") {
                heroImage.setImageResource(R.drawable.hero_captain)
                heroName.text = "Capitão América"
                heroDesc.text = "Você é leal, corajoso e um líder nato."
            } else if (heroId == "strange") {
                heroImage.setImageResource(R.drawable.hero_strange)
                heroName.text = "Doutor Estranho"
                heroDesc.text = "Você é sábio, místico e sempre busca conhecimento."
            } else if (heroId == "thor") {
                heroImage.setImageResource(R.drawable.hero_thor)
                heroName.text = "Thor"
                heroDesc.text = "Você é nobre, corajoso e poderoso."
            } else if (heroId == "blackwidow") {
                heroImage.setImageResource(R.drawable.hero_blackwidow)
                heroName.text = "Viúva Negra"
                heroDesc.text = "Você é estratégica, ágil e sempre está um passo à frente."
            } else if (heroId == "spiderman") {
                heroImage.setImageResource(R.drawable.hero_spiderman)
                heroName.text = "Homem-Aranha"
                heroDesc.text = "Você é jovem, ágil e sempre ajuda o próximo."
            }

            chooseButton.setOnClickListener {
                Log.d("TiebreakerActivity", "Usuário escolheu: $heroId")
                val intent = Intent(this, ResultActivity::class.java)
                intent.putExtra("HERO_ID", heroId)
                startActivity(intent)
                finish()
            }

            cardLayout.addView(heroImage)
            cardLayout.addView(heroName)
            cardLayout.addView(heroDesc)
            cardLayout.addView(chooseButton)

            heroesLinearLayout.addView(cardLayout)
        }
    }
}
