package com.example.ap2

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ap2.R
import com.example.ap2.data.TripRepository
import com.example.ap2.databinding.ActivityAddExpenseBinding
import com.example.ap2.databinding.DialogPersonalChargeBinding
import com.example.ap2.model.Currency
import com.example.ap2.model.CurrencyConverter
import com.example.ap2.model.Expense
import com.example.ap2.model.Money
import com.example.ap2.model.Participant
import com.example.ap2.model.PersonalCharge
import com.example.ap2.ui.expenses.PersonalChargeAdapter
import com.google.android.material.chip.Chip
import java.util.UUID

class AddExpenseActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EXPENSE_ID = "expense_id"
    }

    private lateinit var binding: ActivityAddExpenseBinding
    private val personalCharges = mutableListOf<PersonalCharge>()
    private lateinit var personalChargeAdapter: PersonalChargeAdapter
    private lateinit var participants: List<Participant>
    private var editingExpenseId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        binding.addPersonalChargeButton.setOnClickListener { showPersonalChargeDialog() }
        binding.saveExpenseButton.setOnClickListener { saveExpense() }

        expenseToEdit?.let { populateExpense(it) }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
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
        binding.currencySpinner.adapter = currencyAdapter
        val payerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            participants.map { it.name }
        )
        binding.payerSpinner.adapter = payerAdapter
    }

    private fun setupParticipantsChips() {
        binding.sharedParticipantsGroup.removeAllViews()
        participants.forEach { participant ->
            val chip = com.google.android.material.chip.Chip(this).apply {
                text = participant.name
                isCheckable = true
                isChecked = true
                tag = participant.id
                id = View.generateViewId()
            }
            binding.sharedParticipantsGroup.addView(chip)
        }
    }

    private fun setupPersonalChargesList() {
        personalChargeAdapter = PersonalChargeAdapter(personalCharges) { charge ->
            personalCharges.remove(charge)
            personalChargeAdapter.refresh()
            togglePersonalChargesBlock()
        }
        binding.personalChargesList.layoutManager = LinearLayoutManager(this)
        binding.personalChargesList.adapter = personalChargeAdapter
        togglePersonalChargesBlock()
    }

    private fun togglePersonalChargesBlock() {
        binding.personalChargesList.isVisible = personalCharges.isNotEmpty()
        binding.emptyPersonalCharges.isVisible = personalCharges.isEmpty()
    }

    private fun populateExpense(expense: Expense) {
        binding.titleInput.setText(expense.title)
        binding.amountInput.setText(expense.total.amount.toString())
        val currencyIndex = Currency.values().indexOf(expense.total.currency)
        if (currencyIndex >= 0) {
            binding.currencySpinner.setSelection(currencyIndex)
        }
        val payerIndex = participants.indexOfFirst { it.id == expense.payerId }
        if (payerIndex >= 0) {
            binding.payerSpinner.setSelection(payerIndex)
        }
        setChipSelections(expense.sharedParticipantIds.toSet())
        personalCharges.clear()
        personalCharges.addAll(expense.personalCharges.map { it.copy() })
        personalChargeAdapter.refresh()
        togglePersonalChargesBlock()
    }

    private fun setChipSelections(selectedIds: Set<String>) {
        for (index in 0 until binding.sharedParticipantsGroup.childCount) {
            val chip = binding.sharedParticipantsGroup.getChildAt(index) as? Chip ?: continue
            val participantId = chip.tag as? String
            chip.isChecked = participantId != null && selectedIds.contains(participantId)
        }
    }

    private fun showPersonalChargeDialog() {
        val dialogBinding = DialogPersonalChargeBinding.inflate(layoutInflater)
        dialogBinding.participantSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            participants.map { it.name }
        )
        AlertDialog.Builder(this)
            .setTitle("Adicionar ajuste individual")
            .setView(dialogBinding.root)
            .setPositiveButton("Adicionar") { _, _ ->
                val amount = dialogBinding.amountInput.text?.toString()?.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    Toast.makeText(this, "Valor inválido", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val participant = participants[dialogBinding.participantSpinner.selectedItemPosition]
                val note = dialogBinding.noteInput.text?.toString().orEmpty()
                val currency = binding.currencySpinner.selectedItem as Currency
                personalCharges.add(
                    PersonalCharge(
                        id = UUID.randomUUID().toString(),
                        participantId = participant.id,
                        money = Money(amount, currency),
                        note = note.ifBlank { null }
                    )
                )
                personalChargeAdapter.refresh()
                togglePersonalChargesBlock()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun saveExpense() {
        val title = binding.titleInput.text?.toString().orEmpty()
        val amount = binding.amountInput.text?.toString()?.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Informe um valor válido", Toast.LENGTH_SHORT).show()
            return
        }
        val currency = binding.currencySpinner.selectedItem as Currency
        val money = Money(amount, currency)
        val payer = participants.getOrNull(binding.payerSpinner.selectedItemPosition)
        if (payer == null) {
            Toast.makeText(this, "Selecione quem pagou", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedParticipants = binding.sharedParticipantsGroup.checkedChipIds.mapNotNull { id ->
            val chip = binding.sharedParticipantsGroup.findViewById<com.google.android.material.chip.Chip>(id)
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
