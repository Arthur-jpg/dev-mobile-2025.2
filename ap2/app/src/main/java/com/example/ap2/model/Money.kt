package com.example.ap2.model

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

enum class Currency(val displayName: String, val symbol: String) {
    BRL("Real (BRL)", "R$"),
    USD("Dólar (USD)", "US$"),
    EUR("Euro (EUR)", "€");

    override fun toString(): String = displayName
}

data class Money(
    val amount: Double,
    val currency: Currency
) {
    fun format(): String {
        val rounded = amount.roundTo(2)
        val formatted = if (abs(rounded - rounded.toLong()) < 0.005) {
            String.format("%.0f", rounded)
        } else {
            String.format("%.2f", rounded)
        }
        return "${currency.symbol} $formatted"
    }

    fun convertTo(target: Currency): Money {
        if (currency == target) return this
        val converted = CurrencyConverter.convert(amount, currency, target)
        return Money(converted, target)
    }

    private fun Double.roundTo(decimals: Int): Double {
        val factor = 10.0.pow(decimals)
        return round(this * factor) / factor
    }
}

object CurrencyConverter {
    // Static reference rates (1 unit converted to BRL)
    private val referenceToBrl = mapOf(
        Currency.BRL to 1.0,
        Currency.USD to 5.65,
        Currency.EUR to 6.15
    )

    fun convert(amount: Double, from: Currency, to: Currency): Double {
        if (from == to) return amount
        val fromRate = referenceToBrl[from] ?: error("Missing rate for $from")
        val toRate = referenceToBrl[to] ?: error("Missing rate for $to")
        val inBrl = amount * fromRate
        return inBrl / toRate
    }
}
