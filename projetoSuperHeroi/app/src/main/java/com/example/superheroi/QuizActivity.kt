package com.example.superheroi

import android.content.Intent
import android.util.Log
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class QuizActivity : AppCompatActivity() {
    
    var currentQuestionIndex = 0
    var ironmanScore = 0
    var hulkScore = 0
    var captainScore = 0
    var strangeScore = 0
    var thorScore = 0
    var blackwidowScore = 0
    var spidermanScore = 0

    val questions = listOf(
        "Qual é o seu maior medo?",
        "Como você prefere resolver conflitos?",
        "Qual é o seu superpoder favorito?",
        "O que você mais valoriza em um time?",
        "Qual é a sua cor favorita?",
        "O que você faria em um dia livre?"
    )

    val questionOptions = listOf(
        // Pergunta 1
        listOf("Perder o controle", "Perder minha força", "Decepcionar quem confio", "Não conseguir proteger os outros"),
        // Pergunta 2
        listOf("Com tecnologia e estratégia", "Com força bruta", "Com liderança e companheirismo", "Com magia e conhecimento"),
        // Pergunta 3
        listOf("Inteligência artificial", "Super força", "Resistência extrema", "Manipulação da realidade", "Controle de trovões", "Habilidades de espionagem", "Escalar paredes"),
        // Pergunta 4
        listOf("Inovação", "Poder", "Lealdade", "Sabedoria", "Coragem", "Estratégia", "Juventude"),
        // Pergunta 5
        listOf("Vermelho e dourado", "Verde", "Azul, vermelho e branco", "Azul e vermelho", "Prata", "Preto", "Vermelho e azul"),
        // Pergunta 6
        listOf("Trabalhar em novos projetos", "Malhar e treinar", "Ajudar outras pessoas", "Estudar e meditar", "Explorar novos lugares", "Missões secretas", "Ajudar o bairro")
    )

    val questionHeroes = listOf(
        // Pergunta 1
        listOf("ironman", "hulk", "captain", "strange"),
        // Pergunta 2
        listOf("ironman", "hulk", "captain", "strange"),
        // Pergunta 3
        listOf("ironman", "hulk", "captain", "strange", "thor", "blackwidow", "spiderman"),
        // Pergunta 4
        listOf("ironman", "hulk", "captain", "strange", "thor", "blackwidow", "spiderman"),
        // Pergunta 5
        listOf("ironman", "hulk", "captain", "strange", "thor", "blackwidow", "spiderman"),
        // Pergunta 6
        listOf("ironman", "hulk", "captain", "strange", "thor", "blackwidow", "spiderman")
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)
        
        // Encontrar as views
        val userNameTextView = findViewById<TextView>(R.id.userNameTextView)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val progressTextView = findViewById<TextView>(R.id.progressTextView)
        val questionTextView = findViewById<TextView>(R.id.questionTextView)
        val answersRadioGroup = findViewById<RadioGroup>(R.id.answersRadioGroup)
        val option1RadioButton = findViewById<RadioButton>(R.id.option1RadioButton)
        val option2RadioButton = findViewById<RadioButton>(R.id.option2RadioButton)
        val option3RadioButton = findViewById<RadioButton>(R.id.option3RadioButton)
        val option4RadioButton = findViewById<RadioButton>(R.id.option4RadioButton)
        val option5RadioButton = findViewById<RadioButton>(R.id.option5RadioButton)
        val option6RadioButton = findViewById<RadioButton>(R.id.option6RadioButton)
        val option7RadioButton = findViewById<RadioButton>(R.id.option7RadioButton)
        val nextButton = findViewById<Button>(R.id.nextButton)

        val userName = intent.getStringExtra("USER_NAME")
        userNameTextView.text = "Olá, $userName!"

        progressBar.max = questions.size

        fun showQuestion() {
            val currentQuestion = questions[currentQuestionIndex]
            val currentOptions = questionOptions[currentQuestionIndex]
            
            questionTextView.text = currentQuestion

            val allOptions = listOf(
                option1RadioButton, option2RadioButton, option3RadioButton,
                option4RadioButton, option5RadioButton, option6RadioButton, option7RadioButton
            )

            for (i in allOptions.indices) {
                if (i < currentOptions.size) {
                    allOptions[i].visibility = android.view.View.VISIBLE
                    allOptions[i].text = currentOptions[i]
                } else {
                    allOptions[i].visibility = android.view.View.GONE
                }
            }

            progressBar.progress = currentQuestionIndex + 1
            progressTextView.text = "Pergunta ${currentQuestionIndex + 1} de ${questions.size}"

            answersRadioGroup.clearCheck()

            if (currentQuestionIndex == questions.size - 1) {
                nextButton.text = "Ver Resultado"
            } else {
                nextButton.text = "Próxima"
            }
            
            Log.d("QuizActivity", "Mostrando pergunta ${currentQuestionIndex + 1}")
        }
        showQuestion()
        nextButton.setOnClickListener {
            if (answersRadioGroup.checkedRadioButtonId == -1) {
                Toast.makeText(this, "Por favor, selecione uma resposta!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedIndex = when (answersRadioGroup.checkedRadioButtonId) {
                R.id.option1RadioButton -> 0
                R.id.option2RadioButton -> 1
                R.id.option3RadioButton -> 2
                R.id.option4RadioButton -> 3
                R.id.option5RadioButton -> 4
                R.id.option6RadioButton -> 5
                R.id.option7RadioButton -> 6
                else -> 0
            }
            
            // Adicionar ponto para o herói correspondente
            val selectedHero = questionHeroes[currentQuestionIndex][selectedIndex]
            
            when (selectedHero) {
                "ironman" -> ironmanScore++
                "hulk" -> hulkScore++
                "captain" -> captainScore++
                "strange" -> strangeScore++
                "thor" -> thorScore++
                "blackwidow" -> blackwidowScore++
                "spiderman" -> spidermanScore++
            }
            
            Log.d("QuizActivity", "Resposta: $selectedHero - Pontos: ironman=$ironmanScore, hulk=$hulkScore, captain=$captainScore, strange=$strangeScore, thor=$thorScore, blackwidow=$blackwidowScore, spiderman=$spidermanScore")
            
            // Ir para próxima pergunta ou mostrar resultado
            if (currentQuestionIndex < questions.size - 1) {
                currentQuestionIndex++
                showQuestion()
            } else {
                // Acabou o quiz! Descobrir o vencedor
                val scores = listOf(
                    Pair("ironman", ironmanScore),
                    Pair("hulk", hulkScore),
                    Pair("captain", captainScore),
                    Pair("strange", strangeScore),
                    Pair("thor", thorScore),
                    Pair("blackwidow", blackwidowScore),
                    Pair("spiderman", spidermanScore)
                )
                
                val maxScore = scores.maxByOrNull { it.second }?.second ?: 0
                val winners = scores.filter { it.second == maxScore }.map { it.first }
                
                Log.d("QuizActivity", "Quiz finalizado! Vencedores: $winners")
                
                if (winners.size > 1) {
                    val intent = Intent(this, TiebreakerActivity::class.java)
                    intent.putStringArrayListExtra("TIED_HERO_IDS", ArrayList(winners))
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(this, ResultActivity::class.java)
                    intent.putExtra("HERO_ID", winners[0])
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}
