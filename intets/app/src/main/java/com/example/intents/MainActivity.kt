package com.example.intents

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etNome = findViewById<EditText>(R.id.editTextText)
        val btAvancar = findViewById<Button>(R.id.button2)
        var nome:String = ""
        btAvancar.setOnClickListener {
            nome = etNome.text.toString()
            val intent = Intent(this, Activity2::class.java)
            startActivity(intent)
        }
    }
}