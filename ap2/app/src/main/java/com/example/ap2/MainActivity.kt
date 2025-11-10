package com.example.ap2

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.ap2.data.TripRepository
import com.example.ap2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            TripRepository.clearAll()
        }

        binding.tripNameInput.setText(TripRepository.tripName)
        binding.startButton.setOnClickListener {
            val tripName = binding.tripNameInput.text?.toString().orEmpty()
            TripRepository.setTripName(tripName)
            val intent = Intent(this, ParticipantsActivity::class.java)
            startActivity(intent)
        }
    }
}
