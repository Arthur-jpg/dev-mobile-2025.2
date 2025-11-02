package com.example.superheroi

import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {
    
    private lateinit var nameEditText: TextInputEditText
    private lateinit var termsCheckBox: CheckBox
    private lateinit var startQuizButton: MaterialButton
    private lateinit var logoImageView: ImageView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize views
        nameEditText = findViewById(R.id.nameEditText)
        termsCheckBox = findViewById(R.id.termsCheckBox)
        startQuizButton = findViewById(R.id.startQuizButton)
        logoImageView = findViewById(R.id.logoImageView)
        
        // Set logo
        logoImageView.setImageResource(R.drawable.logo_heroes)
        
        // Set button click listener
        startQuizButton.setOnClickListener {
            startQuiz()
        }
    }
    
    private fun startQuiz() {
        val name = nameEditText.text.toString().trim()
        
        // Validation
        if (name.isEmpty()) {
            Toast.makeText(this, R.string.name_required, Toast.LENGTH_SHORT).show()
            nameEditText.requestFocus()
            return
        }
        
        if (!termsCheckBox.isChecked) {
            Toast.makeText(this, R.string.terms_required, Toast.LENGTH_SHORT).show()
            return
        }
        
        // Start Quiz Activity with user name
        val intent = Intent(this, QuizActivity::class.java)
        intent.putExtra("USER_NAME", name)
        startActivity(intent)
    }
}