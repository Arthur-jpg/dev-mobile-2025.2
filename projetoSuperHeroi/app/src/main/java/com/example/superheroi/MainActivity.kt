package com.example.superheroi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val termsCheckBox = findViewById<CheckBox>(R.id.termsCheckBox)
        val startQuizButton = findViewById<Button>(R.id.startQuizButton)
        val logoImageView = findViewById<ImageView>(R.id.logoImageView)

        logoImageView.setImageResource(R.drawable.logo_heroes)

        startQuizButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Por favor, insira seu nome!", Toast.LENGTH_SHORT).show()
                nameEditText.requestFocus()
                return@setOnClickListener
            }
            
            if (!termsCheckBox.isChecked) {
                Toast.makeText(this, "Você precisa aceitar os termos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra("USER_NAME", name)
            startActivity(intent)
        }
    }
}