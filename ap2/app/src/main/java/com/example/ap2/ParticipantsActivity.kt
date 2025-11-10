package com.example.ap2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ap2.data.TripRepository
import com.example.ap2.databinding.ActivityParticipantsBinding
import com.example.ap2.ui.participants.ParticipantAdapter

class ParticipantsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityParticipantsBinding
    private val adapter = ParticipantAdapter { participant ->
        TripRepository.removeParticipant(participant.id)
        refreshList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParticipantsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.title = TripRepository.tripName
        binding.participantsList.layoutManager = LinearLayoutManager(this)
        binding.participantsList.adapter = adapter

        binding.addParticipantButton.setOnClickListener {
            val name = binding.participantInput.text?.toString().orEmpty()
            if (name.isBlank()) {
                Toast.makeText(this, "Digite um nome antes de adicionar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            runCatching { TripRepository.addParticipant(name) }
                .onSuccess {
                    binding.participantInput.text?.clear()
                    refreshList()
                }
                .onFailure {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
        }

        binding.goToExpensesButton.setOnClickListener {
            if (TripRepository.getParticipants().size < 2) {
                Toast.makeText(this, "Adicione pelo menos duas pessoas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(this, ExpensesActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun refreshList() {
        val participants = TripRepository.getParticipants()
        adapter.submitList(participants)
        binding.emptyState.isVisible = participants.isEmpty()
        binding.goToExpensesButton.isEnabled = participants.size >= 2
    }
}
