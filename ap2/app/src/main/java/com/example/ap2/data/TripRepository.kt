package com.example.ap2.data

import com.example.ap2.model.Currency
import com.example.ap2.model.Expense
import com.example.ap2.model.Money
import com.example.ap2.model.Participant
import com.example.ap2.model.PersonalCharge
import java.util.UUID

object TripRepository {
    private val participants = mutableListOf<Participant>()
    private val expenses = mutableListOf<Expense>()

    var tripName: String = "Minha viagem"
        private set

    var displayCurrency: Currency = Currency.BRL
        private set

    fun setTripName(name: String) {
        tripName = name.ifBlank { "Minha viagem" }
    }

    fun updateDisplayCurrency(currency: Currency) {
        displayCurrency = currency
    }

    fun getParticipants(): List<Participant> = participants.toList()

    fun addParticipant(name: String): Participant {
        require(name.isNotBlank()) { "Nome não pode ser vazio" }
        val exists = participants.any { it.name.equals(name.trim(), ignoreCase = true) }
        if (exists) error("Participante já existe")
        val participant = Participant(name = name.trim())
        participants.add(participant)
        return participant
    }

    fun removeParticipant(id: String) {
        participants.removeAll { it.id == id }
        val updatedExpenses = expenses.map { expense ->
            val filteredShared = expense.sharedParticipantIds.filter { it != id }
            val filteredCharges = expense.personalCharges.filter { it.participantId != id }
            expense.copy(
                sharedParticipantIds = filteredShared,
                personalCharges = filteredCharges
            )
        }
        expenses.clear()
        expenses.addAll(updatedExpenses)
    }

    fun getExpenses(): List<Expense> = expenses.toList()

    fun addExpense(
        title: String,
        payerId: String,
        amount: Money,
        sharedParticipantIds: List<String>,
        personalCharges: List<PersonalCharge>
    ) {
        val cleanedShared = sharedParticipantIds.distinct()
        val validPersonalCharges = personalCharges
            .filter { charge -> participants.any { it.id == charge.participantId } }
        val expense = Expense(
            id = UUID.randomUUID().toString(),
            title = title.trim().ifBlank { "Despesa sem nome" },
            payerId = payerId,
            total = amount,
            sharedParticipantIds = cleanedShared,
            personalCharges = validPersonalCharges
        )
        expenses.add(expense)
    }

    fun clearAll() {
        participants.clear()
        expenses.clear()
        tripName = "Minha viagem"
        displayCurrency = Currency.BRL
    }
}
