package com.example.ap2.data

import android.content.Context
import android.content.SharedPreferences
import com.example.ap2.model.Currency
import com.example.ap2.model.Expense
import com.example.ap2.model.Money
import com.example.ap2.model.Participant
import com.example.ap2.model.PersonalCharge
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object TripRepository {

    private const val PREFS_NAME = "trip_repository"
    private const val KEY_STATE = "trip_state"

    private val participants = mutableListOf<Participant>()
    private val expenses = mutableListOf<Expense>()
    private var preferences: SharedPreferences? = null

    var tripName: String = "Minha viagem"
        private set

    var displayCurrency: Currency = Currency.BRL
        private set

    fun initialize(context: Context) {
        if (preferences != null) return
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadState()
    }

    fun setTripName(name: String) {
        tripName = name.ifBlank { "Minha viagem" }
        persistState()
    }

    fun updateDisplayCurrency(currency: Currency) {
        displayCurrency = currency
        persistState()
    }

    fun getParticipants(): List<Participant> = participants.toList()

    fun addParticipant(name: String): Participant {
        require(name.isNotBlank()) { "Nome não pode ser vazio" }
        val exists = participants.any { it.name.equals(name.trim(), ignoreCase = true) }
        if (exists) error("Participante já existe")
        val participant = Participant(name = name.trim())
        participants.add(participant)
        persistState()
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
        persistState()
    }

    fun getExpenses(): List<Expense> = expenses.toList()

    fun getExpense(id: String): Expense? = expenses.find { it.id == id }

    fun addExpense(
        title: String,
        payerId: String,
        amount: Money,
        sharedParticipantIds: List<String>,
        personalCharges: List<PersonalCharge>
    ) {
        val expense = buildExpense(
            id = UUID.randomUUID().toString(),
            title = title,
            payerId = payerId,
            amount = amount,
            sharedParticipantIds = sharedParticipantIds,
            personalCharges = personalCharges
        )
        expenses.add(expense)
        persistState()
    }

    fun updateExpense(
        id: String,
        title: String,
        payerId: String,
        amount: Money,
        sharedParticipantIds: List<String>,
        personalCharges: List<PersonalCharge>
    ) {
        val index = expenses.indexOfFirst { it.id == id }
        if (index == -1) return
        val updatedExpense = buildExpense(
            id = id,
            title = title,
            payerId = payerId,
            amount = amount,
            sharedParticipantIds = sharedParticipantIds,
            personalCharges = personalCharges
        )
        expenses[index] = updatedExpense
        persistState()
    }

    fun removeExpense(expenseId: String) {
        val changed = expenses.removeAll { it.id == expenseId }
        if (changed) {
            persistState()
        }
    }

    fun clearAll() {
        participants.clear()
        expenses.clear()
        tripName = "Minha viagem"
        displayCurrency = Currency.BRL
        preferences?.edit()?.remove(KEY_STATE)?.apply()
    }

    private fun loadState() {
        val prefs = preferences ?: return
        val saved = prefs.getString(KEY_STATE, null) ?: return
        runCatching {
            val json = JSONObject(saved)
            tripName = json.optString("tripName", "Minha viagem")
            displayCurrency = json.optString("displayCurrency").toCurrency()
            participants.clear()
            val participantsArray = json.optJSONArray("participants") ?: JSONArray()
            for (i in 0 until participantsArray.length()) {
                val item = participantsArray.getJSONObject(i)
                participants.add(
                    Participant(
                        id = item.optString("id", UUID.randomUUID().toString()),
                        name = item.optString("name")
                    )
                )
            }
            expenses.clear()
            val expensesArray = json.optJSONArray("expenses") ?: JSONArray()
            for (i in 0 until expensesArray.length()) {
                val expenseJson = expensesArray.getJSONObject(i)
                expenses += expenseJson.toExpense()
            }
        }.onFailure {
            clearAll()
        }
    }

    private fun buildExpense(
        id: String,
        title: String,
        payerId: String,
        amount: Money,
        sharedParticipantIds: List<String>,
        personalCharges: List<PersonalCharge>
    ): Expense {
        val cleanedShared = sharedParticipantIds.distinct()
        val validPersonalCharges = personalCharges
            .filter { charge -> participants.any { it.id == charge.participantId } }
        return Expense(
            id = id,
            title = title.trim().ifBlank { "Despesa sem nome" },
            payerId = payerId,
            total = amount,
            sharedParticipantIds = cleanedShared,
            personalCharges = validPersonalCharges
        )
    }

    private fun JSONObject.toExpense(): Expense {
        val totalObject = getJSONObject("total")
        val sharedArray = optJSONArray("sharedParticipantIds") ?: JSONArray()
        val sharedIds = mutableListOf<String>()
        for (j in 0 until sharedArray.length()) {
            sharedIds += sharedArray.optString(j)
        }
        val chargesArray = optJSONArray("personalCharges") ?: JSONArray()
        val charges = mutableListOf<PersonalCharge>()
        for (j in 0 until chargesArray.length()) {
            val chargeObject = chargesArray.getJSONObject(j)
            charges += chargeObject.toPersonalCharge()
        }
        return Expense(
            id = optString("id", UUID.randomUUID().toString()),
            title = optString("title"),
            payerId = optString("payerId"),
            total = Money(
                amount = totalObject.optDouble("amount", 0.0),
                currency = totalObject.optString("currency").toCurrency()
            ),
            sharedParticipantIds = sharedIds,
            personalCharges = charges
        )
    }

    private fun JSONObject.toPersonalCharge(): PersonalCharge {
        val moneyObject = getJSONObject("money")
        val noteValue = optStringOrNull("note")
        return PersonalCharge(
            id = optString("id", UUID.randomUUID().toString()),
            participantId = optString("participantId"),
            money = Money(
                amount = moneyObject.optDouble("amount", 0.0),
                currency = moneyObject.optString("currency").toCurrency()
            ),
            note = noteValue
        )
    }

    private fun persistState() {
        val prefs = preferences ?: return
        val json = JSONObject().apply {
            put("tripName", tripName)
            put("displayCurrency", displayCurrency.name)
            put("participants", JSONArray().apply {
                participants.forEach { participant ->
                    put(JSONObject().apply {
                        put("id", participant.id)
                        put("name", participant.name)
                    })
                }
            })
            put("expenses", JSONArray().apply {
                expenses.forEach { expense ->
                    put(expense.toJson())
                }
            })
        }
        prefs.edit().putString(KEY_STATE, json.toString()).apply()
    }

    private fun Expense.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("payerId", payerId)
        put("total", JSONObject().apply {
            put("amount", total.amount)
            put("currency", total.currency.name)
        })
        put("sharedParticipantIds", JSONArray().apply {
            sharedParticipantIds.forEach { put(it) }
        })
        put("personalCharges", JSONArray().apply {
            personalCharges.forEach { charge ->
                put(charge.toJson())
            }
        })
    }

    private fun PersonalCharge.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("participantId", participantId)
        note?.let { put("note", it) }
        put("money", JSONObject().apply {
            put("amount", money.amount)
            put("currency", money.currency.name)
        })
    }

    private fun String?.toCurrency(): Currency =
        runCatching { Currency.valueOf(this ?: Currency.BRL.name) }.getOrDefault(Currency.BRL)

    private fun JSONObject.optStringOrNull(name: String): String? =
        if (has(name) && !isNull(name)) optString(name) else null
}
