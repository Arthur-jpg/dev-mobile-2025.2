package com.example.ap2

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import com.example.ap2.data.TripRepository
import com.example.ap2.model.Currency
import com.example.ap2.model.CurrencyConverter
import com.example.ap2.model.Money
import com.example.ap2.ui.expenses.ExpensesListFragment
import com.google.android.material.appbar.MaterialToolbar

class OverviewActivity : AppCompatActivity() {

    private val toolbar: MaterialToolbar
        get() = findViewById(R.id.toolbar)
    private val totalValue: TextView
        get() = findViewById(R.id.overviewTotalValue)
    private val participantsValue: TextView
        get() = findViewById(R.id.overviewParticipantsValue)
    private val fragmentContainer: FragmentContainerView
        get() = findViewById(R.id.fragmentContainer)
    private val currencySpinner: Spinner
        get() = findViewById(R.id.overviewCurrencySpinner)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overview)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.overview_title)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(fragmentContainer.id, ExpensesListFragment())
                .commit()
        }

        setupCurrencySpinner()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onResume() {
        super.onResume()
        updateSummary()
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
        currencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val currency = parent?.getItemAtPosition(position) as? Currency ?: return
                TripRepository.updateDisplayCurrency(currency)
                updateSummary()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun updateSummary() {
        val participants = TripRepository.getParticipants()
        val expenses = TripRepository.getExpenses()
        val currency = TripRepository.displayCurrency

        val totalSpent = expenses.sumOf { expense ->
            val base = CurrencyConverter.convert(
                expense.total.amount,
                expense.total.currency,
                Currency.BRL
            )
            base
        }
        val formattedTotal = Money(totalSpent, Currency.BRL).convertTo(currency).format()

        totalValue.text = formattedTotal
        participantsValue.text = getString(
            R.string.overview_participants_value,
            participants.size
        )
    }
}
