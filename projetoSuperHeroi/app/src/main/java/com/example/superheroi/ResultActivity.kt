package com.example.superheroi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val heroImageView = findViewById<ImageView>(R.id.heroImageView)
        val heroNameTextView = findViewById<TextView>(R.id.heroNameTextView)
        val heroDescriptionTextView = findViewById<TextView>(R.id.heroDescriptionTextView)
        val shareButton = findViewById<Button>(R.id.shareButton)
        val githubButton = findViewById<Button>(R.id.githubButton)
        val restartButton = findViewById<Button>(R.id.restartButton)

        val heroId = intent.getStringExtra("HERO_ID")

        if (heroId == "ironman") {
            heroImageView.setImageResource(R.drawable.hero_ironman)
            heroNameTextView.text = "Homem de Ferro"
            heroDescriptionTextView.text = "Você é inovador, inteligente e estratégico. Sempre busca soluções tecnológicas e lidera com confiança."
        } else if (heroId == "hulk") {
            heroImageView.setImageResource(R.drawable.hero_hulk)
            heroNameTextView.text = "Hulk"
            heroDescriptionTextView.text = "Você é forte, poderoso e determinado. Quando necessário, você mostra toda sua força interior."
        } else if (heroId == "captain") {
            heroImageView.setImageResource(R.drawable.hero_captain)
            heroNameTextView.text = "Capitão América"
            heroDescriptionTextView.text = "Você é leal, corajoso e um líder nato. Sempre defende o que é certo e inspira os outros."
        } else if (heroId == "strange") {
            heroImageView.setImageResource(R.drawable.hero_strange)
            heroNameTextView.text = "Doutor Estranho"
            heroDescriptionTextView.text = "Você é sábio, místico e sempre busca conhecimento. Usa sua inteligência para proteger os outros."
        } else if (heroId == "thor") {
            heroImageView.setImageResource(R.drawable.hero_thor)
            heroNameTextView.text = "Thor"
            heroDescriptionTextView.text = "Você é nobre, corajoso e poderoso. Enfrenta qualquer desafio com bravura e honra."
        } else if (heroId == "blackwidow") {
            heroImageView.setImageResource(R.drawable.hero_blackwidow)
            heroNameTextView.text = "Viúva Negra"
            heroDescriptionTextView.text = "Você é estratégica, ágil e sempre está um passo à frente. Suas habilidades são incomparáveis."
        } else if (heroId == "spiderman") {
            heroImageView.setImageResource(R.drawable.hero_spiderman)
            heroNameTextView.text = "Homem-Aranha"
            heroDescriptionTextView.text = "Você é jovem, ágil e sempre ajuda o próximo. Usa seus poderes para proteger sua comunidade."
        }

        shareButton.setOnClickListener {
            val heroName = heroNameTextView.text.toString()
            val shareText = "Eu fiz o quiz de super-heróis e meu resultado foi: $heroName! Faça você também!"
            
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, "Quiz Super Herói")
            intent.putExtra(Intent.EXTRA_TEXT, shareText)
            
            startActivity(Intent.createChooser(intent, "Compartilhar via"))
        }

        githubButton.setOnClickListener {
            val githubUrl = "https://github.com/Arthur-jpg/dev-mobile-2025.2/tree/main/projetoSuperHeroi"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
            startActivity(intent)
        }

        restartButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
