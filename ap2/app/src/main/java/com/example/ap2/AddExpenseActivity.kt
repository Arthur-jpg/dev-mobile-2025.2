package com.example.ap2

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ap2.data.TripRepository
import com.example.ap2.model.Currency
import com.example.ap2.model.CurrencyConverter
import com.example.ap2.model.Expense
import com.example.ap2.model.Money
import com.example.ap2.model.Participant
import com.example.ap2.model.PersonalCharge
import com.example.ap2.ui.expenses.PersonalChargeAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import java.util.UUID

class AddExpenseActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EXPENSE_ID = "expense_id"
    }

    private val toolbar: MaterialToolbar
        get() = findViewById(R.id.toolbar)
    private val titleInput: TextInputEditText
        get() = findViewById(R.id.titleInput)
    private val amountInput: TextInputEditText
        get() = findViewById(R.id.amountInput)
    private val currencySpinner: Spinner
        get() = findViewById(R.id.currencySpinner)
    private val payerSpinner: Spinner
        get() = findViewById(R.id.payerSpinner)
    private val sharedParticipantsGroup: ChipGroup
        get() = findViewById(R.id.sharedParticipantsGroup)
    private val emptyPersonalCharges: TextView
        get() = findViewById(R.id.emptyPersonalCharges)
    private val personalChargesList: RecyclerView
        get() = findViewById(R.id.personalChargesList)
    private val addPersonalChargeButton: MaterialButton
        get() = findViewById(R.id.addPersonalChargeButton)
    private val saveExpenseButton: MaterialButton
        get() = findViewById(R.id.saveExpenseButton)

    private val personalCharges = mutableListOf<PersonalCharge>()
    private var personalChargeAdapter: PersonalChargeAdapter? = null
    private var participants: List<Participant> = emptyList()
    private var editingExpenseId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        participants = TripRepository.getParticipants()
        if (participants.isEmpty()) {
            Toast.makeText(this, "Adicione participantes antes", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        editingExpenseId = intent.getStringExtra(EXTRA_EXPENSE_ID)
        val expenseToEdit = editingExpenseId?.let { TripRepository.getExpense(it) }
        if (editingExpenseId != null && expenseToEdit == null) {
            Toast.makeText(this, "Despesa não encontrada", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupSpinners()
        setupParticipantsChips()
        setupPersonalChargesList()

        addPersonalChargeButton.setOnClickListener { showPersonalChargeDialog() }
        saveExpenseButton.setOnClickListener { saveExpense() }

        expenseToEdit?.let { populateExpense(it) }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val titleRes = if (editingExpenseId == null) {
            R.string.add_expense_new_title
        } else {
            R.string.add_expense_edit_title
        }
        supportActionBar?.title = getString(titleRes)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupSpinners() {
        val currencyAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            Currency.values().toList()
        )
        currencySpinner.adapter = currencyAdapter
        val payerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            participants.map { it.name }
        )
        payerSpinner.adapter = payerAdapter
    }

    private fun setupParticipantsChips() {
        sharedParticipantsGroup.removeAllViews()
        participants.forEach { participant ->
            val chip = Chip(this).apply {
                text = participant.name
                isCheckable = true
                isChecked = true
                tag = participant.id
                id = View.generateViewId()
            }
            sharedParticipantsGroup.addView(chip)
        }
    }

    private fun setupPersonalChargesList() {
        personalChargeAdapter = PersonalChargeAdapter(personalCharges) { charge ->
            personalCharges.remove(charge)
            personalChargeAdapter?.refresh()
            togglePersonalChargesBlock()
        }
        personalChargesList.layoutManager = LinearLayoutManager(this)
        personalChargesList.adapter = personalChargeAdapter
        togglePersonalChargesBlock()
    }

    private fun togglePersonalChargesBlock() {
        personalChargesList.isVisible = personalCharges.isNotEmpty()
        emptyPersonalCharges.isVisible = personalCharges.isEmpty()
    }

    private fun populateExpense(expense: Expense) {
        titleInput.setText(expense.title)
        amountInput.setText(expense.total.amount.toString())
        val currencyIndex = Currency.values().indexOf(expense.total.currency)
        if (currencyIndex >= 0) {
            currencySpinner.setSelection(currencyIndex)
        }
        val payerIndex = participants.indexOfFirst { it.id == expense.payerId }
        if (payerIndex >= 0) {
            payerSpinner.setSelection(payerIndex)
        }
        setChipSelections(expense.sharedParticipantIds.toSet())
        personalCharges.clear()
        personalCharges.addAll(expense.personalCharges.map { it.copy() })
        personalChargeAdapter?.refresh()
        togglePersonalChargesBlock()
    }

    private fun setChipSelections(selectedIds: Set<String>) {
        for (index in 0 until sharedParticipantsGroup.childCount) {
            val chip = sharedParticipantsGroup.getChildAt(index) as? Chip ?: continue
            val participantId = chip.tag as? String
            chip.isChecked = participantId != null && selectedIds.contains(participantId)
        }
    }

    private fun showPersonalChargeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_personal_charge, null)
        val participantSpinner: Spinner = dialogView.findViewById(R.id.participantSpinner)
        val amountInputField: TextInputEditText = dialogView.findViewById(R.id.amountInput)
        val noteInputField: TextInputEditText = dialogView.findViewById(R.id.noteInput)

        participantSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            participants.map { it.name }
        )

        AlertDialog.Builder(this)
            .setTitle("Adicionar ajuste individual")
            .setView(dialogView)
            .setPositiveButton("Adicionar") { _, _ ->
                val amount = amountInputField.text?.toString()?.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    Toast.makeText(this, "Valor inválido", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val participant = participants[participantSpinner.selectedItemPosition]
                val note = noteInputField.text?.toString().orEmpty()
                val currency = currencySpinner.selectedItem as Currency
                personalCharges.add(
                    PersonalCharge(
                        id = UUID.randomUUID().toString(),
                        participantId = participant.id,
                        money = Money(amount, currency),
                        note = note.ifBlank { null }
                    )
                )
        personalChargeAdapter?.refresh()
        togglePersonalChargesBlock()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun saveExpense() {
        val title = titleInput.text?.toString().orEmpty()
        val amount = amountInput.text?.toString()?.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Informe um valor válido", Toast.LENGTH_SHORT).show()
            return
        }
        val currency = currencySpinner.selectedItem as Currency
        val money = Money(amount, currency)
        val payer = participants.getOrNull(payerSpinner.selectedItemPosition)
        if (payer == null) {
            Toast.makeText(this, "Selecione quem pagou", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedParticipants = sharedParticipantsGroup.checkedChipIds.mapNotNull { id ->
            val chip = sharedParticipantsGroup.findViewById<Chip>(id)
            chip?.tag as? String
        }

        val totalBase = CurrencyConverter.convert(amount, currency, Currency.BRL)
        val chargesBase = personalCharges.sumOf {
            CurrencyConverter.convert(it.money.amount, it.money.currency, Currency.BRL)
        }
        val sharedPool = (totalBase - chargesBase).coerceAtLeast(0.0)
        if (sharedPool > 0 && selectedParticipants.isEmpty()) {
            Toast.makeText(this, "Selecione pelo menos uma pessoa para dividir", Toast.LENGTH_SHORT).show()
            return
        }
        if (chargesBase - totalBase > 0.01) {
            Toast.makeText(this, "Ajustes individuais não podem passar o total", Toast.LENGTH_SHORT).show()
            return
        }

        val personalChargesCopy = personalCharges.toList()
        if (editingExpenseId == null) {
            TripRepository.addExpense(
                title = title,
                payerId = payer.id,
                amount = money,
                sharedParticipantIds = selectedParticipants,
                personalCharges = personalChargesCopy
            )
            Toast.makeText(this, "Despesa salva!", Toast.LENGTH_SHORT).show()
        } else {
            TripRepository.updateExpense(
                id = editingExpenseId!!,
                title = title,
                payerId = payer.id,
                amount = money,
                sharedParticipantIds = selectedParticipants,
                personalCharges = personalChargesCopy
            )
            Toast.makeText(this, "Despesa atualizada!", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}
