package com.example.ap2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ap2.data.TripRepository
import com.example.ap2.ui.participants.ParticipantAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ParticipantsActivity : AppCompatActivity() {

    private val adapter = ParticipantAdapter { participant ->
        TripRepository.removeParticipant(participant.id)
        refreshList()
    }

    private val toolbar: MaterialToolbar
        get() = findViewById(R.id.toolbar)
    private val participantsList
        get() = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.participantsList)
    private val participantInput: TextInputEditText
        get() = findViewById(R.id.participantInput)
    private val addParticipantButton: MaterialButton
        get() = findViewById(R.id.addParticipantButton)
    private val goToExpensesButton: MaterialButton
        get() = findViewById(R.id.goToExpensesButton)
    private val emptyState
        get() = findViewById<android.widget.TextView>(R.id.emptyState)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_participants)

        toolbar.title = TripRepository.tripName
        participantsList.layoutManager = LinearLayoutManager(this)
        participantsList.adapter = adapter

        addParticipantButton.setOnClickListener {
            val name = participantInput.text?.toString().orEmpty()
            if (name.isBlank()) {
                Toast.makeText(this, "Digite um nome antes de adicionar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            runCatching { TripRepository.addParticipant(name) }
                .onSuccess {
                    participantInput.text?.clear()
                    refreshList()
                }
                .onFailure {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
        }

        goToExpensesButton.setOnClickListener {
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
        emptyState.isVisible = participants.isEmpty()
        goToExpensesButton.isEnabled = participants.size >= 2
    }
}
