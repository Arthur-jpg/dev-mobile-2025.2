package com.example.superheroi

import android.content.Intent
import android.util.Log
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class QuizActivity : AppCompatActivity() {
    
    private lateinit var userNameTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressTextView: TextView
    private lateinit var questionTextView: TextView
    private lateinit var answersRadioGroup: RadioGroup
    private lateinit var option1RadioButton: RadioButton
    private lateinit var option2RadioButton: RadioButton
    private lateinit var option3RadioButton: RadioButton
    private lateinit var option4RadioButton: RadioButton
    private lateinit var option5RadioButton: RadioButton
    private lateinit var option6RadioButton: RadioButton
    private lateinit var option7RadioButton: RadioButton
    private lateinit var nextButton: MaterialButton
    
    private val allRadioButtons by lazy {
        listOf(option1RadioButton, option2RadioButton, option3RadioButton, 
               option4RadioButton, option5RadioButton, option6RadioButton, option7RadioButton)
    }
    
    private var currentQuestionIndex = 0
    private val heroScores = mutableMapOf(
        "ironman" to 0,
        "hulk" to 0,
        "captain" to 0,
        "strange" to 0,
        "thor" to 0,
        "blackwidow" to 0,
        "spiderman" to 0
    )
    
    // Stable order for saving/restoring scores
    private val HERO_IDS = listOf("ironman", "hulk", "captain", "strange", "thor", "blackwidow", "spiderman")
    
    companion object {
        private const val TAG = "QuizActivity"
    }
    
    private data class Question(
        val text: Int,
        val options: List<Int>,
        val scores: List<String> // hero IDs in order of options
    )
    
    private val questions = listOf(
        Question(
            R.string.question_1,
            listOf(R.string.q1_option1, R.string.q1_option2, R.string.q1_option3, R.string.q1_option4),
            listOf("ironman", "hulk", "captain", "strange")
        ),
        Question(
            R.string.question_2,
            listOf(R.string.q2_option1, R.string.q2_option2, R.string.q2_option3, R.string.q2_option4),
            listOf("ironman", "hulk", "captain", "strange")
        ),
        Question(
            R.string.question_3,
            listOf(R.string.q3_option1, R.string.q3_option2, R.string.q3_option3, R.string.q3_option4, R.string.q3_option5, R.string.q3_option6, R.string.q3_option7),
            listOf("ironman", "hulk", "captain", "strange", "thor", "blackwidow", "spiderman")
        ),
        Question(
            R.string.question_4,
            listOf(R.string.q4_option1, R.string.q4_option2, R.string.q4_option3, R.string.q4_option4, R.string.q4_option5, R.string.q4_option6, R.string.q4_option7),
            listOf("ironman", "hulk", "captain", "strange", "thor", "blackwidow", "spiderman")
        ),
        Question(
            R.string.question_5,
            listOf(R.string.q5_option1, R.string.q5_option2, R.string.q5_option3, R.string.q5_option4, R.string.q5_option5, R.string.q5_option6, R.string.q5_option7),
            listOf("ironman", "hulk", "captain", "strange", "thor", "blackwidow", "spiderman")
        ),
        Question(
            R.string.question_6,
            listOf(R.string.q6_option1, R.string.q6_option2, R.string.q6_option3, R.string.q6_option4, R.string.q6_option5, R.string.q6_option6, R.string.q6_option7),
            listOf("ironman", "hulk", "captain", "strange", "thor", "blackwidow", "spiderman")
        )
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)
        
        // Initialize views
        userNameTextView = findViewById(R.id.userNameTextView)
        progressBar = findViewById(R.id.progressBar)
        progressTextView = findViewById(R.id.progressTextView)
        questionTextView = findViewById(R.id.questionTextView)
        answersRadioGroup = findViewById(R.id.answersRadioGroup)
        option1RadioButton = findViewById(R.id.option1RadioButton)
        option2RadioButton = findViewById(R.id.option2RadioButton)
        option3RadioButton = findViewById(R.id.option3RadioButton)
        option4RadioButton = findViewById(R.id.option4RadioButton)
        option5RadioButton = findViewById(R.id.option5RadioButton)
        option6RadioButton = findViewById(R.id.option6RadioButton)
        option7RadioButton = findViewById(R.id.option7RadioButton)
        nextButton = findViewById(R.id.nextButton)
        
        // Get user name from intent
        val userName = intent.getStringExtra("USER_NAME") ?: "Usuário"
        userNameTextView.text = "Olá, $userName!"
        
        // Setup progress bar
        progressBar.max = questions.size

        // Restore state if available
        if (savedInstanceState != null) {
            currentQuestionIndex = savedInstanceState.getInt("CURRENT_INDEX", 0)
            val savedScores = savedInstanceState.getIntArray("HERO_SCORES")
            if (savedScores != null && savedScores.size == HERO_IDS.size) {
                HERO_IDS.forEachIndexed { idx, id ->
                    heroScores[id] = savedScores[idx]
                }
            }
        }

        // Load current question
        loadQuestion()

        // Restore selected radio (if any)
        if (savedInstanceState != null) {
            val checkedId = savedInstanceState.getInt("CHECKED_ID", -1)
            if (checkedId != -1) {
                // Delay checking until after view is laid out
                answersRadioGroup.post {
                    try {
                        answersRadioGroup.check(checkedId)
                    } catch (e: Exception) {
                        // ignore if not available
                    }
                }
            }
        }
        
        // Set button click listener
        nextButton.setOnClickListener {
            handleNextButton()
        }

        // Log selection live when user taps an option (shows potential hero mapping)
        answersRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val idx = when (checkedId) {
                R.id.option1RadioButton -> 0
                R.id.option2RadioButton -> 1
                R.id.option3RadioButton -> 2
                R.id.option4RadioButton -> 3
                R.id.option5RadioButton -> 4
                R.id.option6RadioButton -> 5
                R.id.option7RadioButton -> 6
                else -> -1
            }
            if (idx >= 0 && idx < questions[currentQuestionIndex].scores.size) {
                val mappedHero = questions[currentQuestionIndex].scores[idx]
                Log.d(TAG, "Selected option index=$idx -> hero=$mappedHero (currentScores=$heroScores)")
            } else {
                Log.d(TAG, "Selected option id=$checkedId (no mapped hero for this index)")
            }
        }
    }
    
    private fun loadQuestion() {
        val question = questions[currentQuestionIndex]
        
        // Update UI
        questionTextView.text = getString(question.text)
        
        // Show/hide options based on question
        allRadioButtons.forEachIndexed { index, radioButton ->
            if (index < question.options.size) {
                radioButton.visibility = android.view.View.VISIBLE
                radioButton.text = getString(question.options[index])
            } else {
                radioButton.visibility = android.view.View.GONE
            }
        }
        
        // Update progress
        progressBar.progress = currentQuestionIndex + 1
        progressTextView.text = "Pergunta ${currentQuestionIndex + 1} de ${questions.size}"

    // Log current question and mapping info
    Log.d(TAG, "Loading question ${currentQuestionIndex + 1}/${questions.size} (options=${question.options.size}) scores=${question.scores}")
        
        // Clear selection
        answersRadioGroup.clearCheck()
        
        // Update button text
        if (currentQuestionIndex == questions.size - 1) {
            nextButton.text = getString(R.string.see_result)
            nextButton.icon = null
        } else {
            nextButton.text = getString(R.string.next_question)
        }
    }
    
    private fun handleNextButton() {
        // Check if an option is selected
        if (answersRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(this, R.string.please_answer, Toast.LENGTH_SHORT).show()
            return
        }
        
        // Record answer
        val selectedOptionIndex = when (answersRadioGroup.checkedRadioButtonId) {
            R.id.option1RadioButton -> 0
            R.id.option2RadioButton -> 1
            R.id.option3RadioButton -> 2
            R.id.option4RadioButton -> 3
            R.id.option5RadioButton -> 4
            R.id.option6RadioButton -> 5
            R.id.option7RadioButton -> 6
            else -> 0
        }
        
        val heroId = questions[currentQuestionIndex].scores[selectedOptionIndex]
        heroScores[heroId] = heroScores[heroId]!! + 1
    Log.d(TAG, "Recorded answer -> hero=$heroId; updatedScores=$heroScores")
        
        // Move to next question or show result
        if (currentQuestionIndex < questions.size - 1) {
            currentQuestionIndex++
            loadQuestion()
        } else {
            showResult()
        }
    }
    
    private fun showResult() {
        // Find maximum score
        val maxScore = heroScores.values.maxOrNull() ?: 0
        
        // Find all heroes with the maximum score (tied heroes)
        val tiedHeroes = heroScores.filter { it.value == maxScore }.keys.toList()
        
        Log.d(TAG, "Quiz completed. Final scores: $heroScores")
        Log.d(TAG, "Max score: $maxScore, Tied heroes: $tiedHeroes")
        
        if (tiedHeroes.size > 1) {
            // Multiple heroes tied - go to tiebreaker screen
            Log.d(TAG, "Tie detected! Navigating to TiebreakerActivity")
            val intent = Intent(this, TiebreakerActivity::class.java)
            intent.putStringArrayListExtra("TIED_HERO_IDS", ArrayList(tiedHeroes))
            startActivity(intent)
            finish()
        } else {
            // Single winner - go directly to result
            val winningHero = tiedHeroes.firstOrNull() ?: "ironman"
            Log.d(TAG, "Winner: $winningHero")
            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra("HERO_ID", winningHero)
            startActivity(intent)
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("CURRENT_INDEX", currentQuestionIndex)
        val scores = IntArray(HERO_IDS.size) { idx -> heroScores[HERO_IDS[idx]] ?: 0 }
        outState.putIntArray("HERO_SCORES", scores)
        outState.putInt("CHECKED_ID", answersRadioGroup.checkedRadioButtonId)
    }
}
