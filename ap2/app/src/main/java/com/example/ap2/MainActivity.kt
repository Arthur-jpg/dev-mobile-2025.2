package com.example.ap2

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.example.ap2.data.TripRepository

class MainActivity : AppCompatActivity() {

    private val tripNameInput: TextInputEditText
        get() = findViewById(R.id.tripNameInput)
    private val startButton: MaterialButton
        get() = findViewById(R.id.startButton)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        tripNameInput.setText(TripRepository.tripName)
        startButton.setOnClickListener {
            val tripName = tripNameInput.text?.toString().orEmpty()
            TripRepository.setTripName(tripName)
            val intent = Intent(this, ParticipantsActivity::class.java)
            startActivity(intent)
        }
    }
}
