package com.example.ap2

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.view.isVisible
import com.example.ap2.data.TripRepository
import com.example.ap2.databinding.ActivitySettlementBinding
import com.example.ap2.domain.ExpenseCalculator
import com.example.ap2.model.Currency
import com.example.ap2.model.Money
import com.example.ap2.model.Transfer
import com.example.ap2.ui.settlement.TransferAdapter

class SettlementActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettlementBinding
    private lateinit var adapter: TransferAdapter
    private var transfers: List<Transfer> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettlementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Fechamento"

        adapter = TransferAdapter(mutableListOf())
        binding.transfersList.layoutManager = LinearLayoutManager(this)
        binding.transfersList.adapter = adapter

        setupCurrencySpinner()
        binding.shareButton.setOnClickListener { shareSummary() }
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
        binding.currencySpinner.adapter = currencyAdapter
        val currentIndex = Currency.values().indexOf(TripRepository.displayCurrency)
        binding.currencySpinner.setSelection(currentIndex)
        binding.currencySpinner.setOnItemSelectedListener<Currency> { currency ->
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
        binding.totalValue.text = totalMoney.format()
        val perPerson = if (participants.isEmpty()) 0.0 else summary.totalSpent / participants.size
        binding.perPersonValue.text = Money(perPerson, Currency.BRL).convertTo(currency).format()

        val balanceLines = summary.balances.entries.joinToString("\n") { (participant, balance) ->
            when {
                balance > 0.01 -> "${participant.name} deve receber ${Money(balance, Currency.BRL).convertTo(currency).format()}"
                balance < -0.01 -> "${participant.name} deve pagar ${Money(-balance, Currency.BRL).convertTo(currency).format()}"
                else -> "${participant.name} está equilibrado"
            }
        }
        binding.balanceSummary.text = balanceLines

        transfers = summary.transfers
        adapter.replace(transfers)
        adapter.displayCurrency = currency
        binding.noTransfersText.isVisible = transfers.isEmpty()
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

    private fun <T> android.widget.Spinner.setOnItemSelectedListener(block: (T) -> Unit) {
        onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                @Suppress("UNCHECKED_CAST")
                block(selectedItem as T)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
        }
    }
}
