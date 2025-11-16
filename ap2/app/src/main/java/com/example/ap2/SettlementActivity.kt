package com.example.ap2

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.isVisible
import com.example.ap2.data.TripRepository
import com.example.ap2.domain.ExpenseCalculator
import com.example.ap2.extensions.onItemSelected
import com.example.ap2.model.Currency
import com.example.ap2.model.Money
import com.example.ap2.model.Transfer
import com.example.ap2.ui.settlement.TransferAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class SettlementActivity : AppCompatActivity() {

    private val toolbar: MaterialToolbar
        get() = findViewById(R.id.toolbar)
    private val currencySpinner: Spinner
        get() = findViewById(R.id.currencySpinner)
    private val totalValue: TextView
        get() = findViewById(R.id.totalValue)
    private val perPersonValue: TextView
        get() = findViewById(R.id.perPersonValue)
    private val balanceSummary: TextView
        get() = findViewById(R.id.balanceSummary)
    private val transfersList: RecyclerView
        get() = findViewById(R.id.transfersList)
    private val noTransfersText: TextView
        get() = findViewById(R.id.noTransfersText)
    private val shareButton: MaterialButton
        get() = findViewById(R.id.shareButton)
    private val adapter = TransferAdapter(mutableListOf())
    private var transfers: List<Transfer> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settlement)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Fechamento"

        transfersList.layoutManager = LinearLayoutManager(this)
        transfersList.adapter = adapter

        setupCurrencySpinner()
        shareButton.setOnClickListener { shareSummary() }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onResume() {
        super.onResume()
        refreshSummary()
    }

    private fun setupCurrencySpinner() {
        val currencyAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            Currency.values().toList()
        )
        currencySpinner.adapter = currencyAdapter
        val currentIndex = Currency.values().indexOf(TripRepository.displayCurrency)
        currencySpinner.setSelection(currentIndex)
        currencySpinner.onItemSelected<Currency> { currency ->
            TripRepository.updateDisplayCurrency(currency)
            adapter.displayCurrency = currency
            refreshSummary()
        }
    }

    private fun refreshSummary() {
        val participants = TripRepository.getParticipants()
        if (participants.isEmpty()) {
            Toast.makeText(this, "Nenhum participante cadastrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val expenses = TripRepository.getExpenses()
        val summary = ExpenseCalculator.buildSummary(participants, expenses)
        val currency = TripRepository.displayCurrency

        val totalMoney = Money(summary.totalSpent, Currency.BRL).convertTo(currency)
        totalValue.text = totalMoney.format()
        val perPerson = if (participants.isEmpty()) 0.0 else summary.totalSpent / participants.size
        perPersonValue.text = Money(perPerson, Currency.BRL).convertTo(currency).format()

        val balanceLines = summary.balances.entries.joinToString("\n") { (participant, balance) ->
            when {
                balance > 0.01 -> "${participant.name} deve receber ${Money(balance, Currency.BRL).convertTo(currency).format()}"
                balance < -0.01 -> "${participant.name} deve pagar ${Money(-balance, Currency.BRL).convertTo(currency).format()}"
                else -> "${participant.name} está equilibrado"
            }
        }
        balanceSummary.text = balanceLines

        transfers = summary.transfers
        adapter.replace(transfers)
        adapter.displayCurrency = currency
        noTransfersText.isVisible = transfers.isEmpty()
    }

    private fun shareSummary() {
        if (transfers.isEmpty()) {
            Toast.makeText(this, "Nada para compartilhar ainda", Toast.LENGTH_SHORT).show()
            return
        }
        val currency = TripRepository.displayCurrency
        val description = transfers.joinToString("\n") { transfer ->
            val amount = transfer.amount.convertTo(currency).format()
            "${transfer.from.name} paga $amount para ${transfer.to.name}"
        }
        val shareText = """
            ${TripRepository.tripName} - Fechamento
            
            $description
        """.trimIndent()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Compartilhar resumo"))
    }

}
