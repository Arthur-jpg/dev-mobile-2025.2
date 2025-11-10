package com.example.ap2.domain

import com.example.ap2.model.Currency
import com.example.ap2.model.CurrencyConverter
import com.example.ap2.model.Expense
import com.example.ap2.model.Money
import com.example.ap2.model.Participant
import com.example.ap2.model.Transfer
import kotlin.math.abs
import kotlin.math.min

object ExpenseCalculator {

    private val baseCurrency = Currency.BRL

    data class SettlementSummary(
        val balances: Map<Participant, Double>,
        val transfers: List<Transfer>,
        val totalSpent: Double
    )

    fun buildSummary(
        participants: List<Participant>,
        expenses: List<Expense>
    ): SettlementSummary {
        if (participants.isEmpty()) {
            return SettlementSummary(emptyMap(), emptyList(), 0.0)
        }
        val balanceMap = participants.associateWith { 0.0 }.toMutableMap()
        var totalSpent = 0.0

        expenses.forEach { expense ->
            val payer = participants.find { it.id == expense.payerId } ?: return@forEach

            val totalBase = CurrencyConverter.convert(
                expense.total.amount,
                expense.total.currency,
                baseCurrency
            )
            totalSpent += totalBase

            val personalSum = expense.personalCharges.sumOf { charge ->
                CurrencyConverter.convert(
                    charge.money.amount,
                    charge.money.currency,
                    baseCurrency
                )
            }
            val sharedPool = (totalBase - personalSum).coerceAtLeast(0.0)
            val sharedParticipants = expense.sharedParticipantIds
                .mapNotNull { id -> participants.find { it.id == id } }
            val shareValue = if (sharedParticipants.isNotEmpty()) {
                sharedPool / sharedParticipants.size
            } else 0.0

            sharedParticipants.forEach { participant ->
                balanceMap[participant] = (balanceMap[participant] ?: 0.0) - shareValue
            }

            expense.personalCharges.forEach { charge ->
                val participant = participants.find { it.id == charge.participantId } ?: return@forEach
                val chargeBase = CurrencyConverter.convert(
                    charge.money.amount,
                    charge.money.currency,
                    baseCurrency
                )
                balanceMap[participant] = (balanceMap[participant] ?: 0.0) - chargeBase
            }

            balanceMap[payer] = (balanceMap[payer] ?: 0.0) + totalBase
        }

        val debtors = mutableListOf<Pair<Participant, Double>>()
        val creditors = mutableListOf<Pair<Participant, Double>>()

        balanceMap.forEach { (participant, balance) ->
            when {
                balance < -0.01 -> debtors += participant to abs(balance)
                balance > 0.01 -> creditors += participant to balance
            }
        }

        val transfers = mutableListOf<Transfer>()
        var debtorIndex = 0
        var creditorIndex = 0

        while (debtorIndex < debtors.size && creditorIndex < creditors.size) {
            val (debtor, debtAmount) = debtors[debtorIndex]
            val (creditor, creditAmount) = creditors[creditorIndex]
            val settledAmount = min(debtAmount, creditAmount)

            transfers += Transfer(
                from = debtor,
                to = creditor,
                amount = Money(settledAmount, baseCurrency)
            )

            debtors[debtorIndex] = debtor to (debtAmount - settledAmount)
            creditors[creditorIndex] = creditor to (creditAmount - settledAmount)

            if (debtors[debtorIndex].second <= 0.01) debtorIndex++
            if (creditors[creditorIndex].second <= 0.01) creditorIndex++
        }

        return SettlementSummary(balanceMap, transfers, totalSpent)
    }
}
