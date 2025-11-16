package com.example.ap2.ui.expenses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageButton
import android.widget.TextView
import com.example.ap2.R
import com.example.ap2.data.TripRepository
import com.example.ap2.model.Currency
import com.example.ap2.model.CurrencyConverter
import com.example.ap2.model.Expense
import com.example.ap2.model.Money

class ExpenseAdapter(
    private val onEdit: (Expense) -> Unit,
    private val onDelete: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(Diff) {

    var displayCurrency: Currency = TripRepository.displayCurrency
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view, onEdit, onDelete)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position), displayCurrency)
    }

    class ExpenseViewHolder(
        itemView: View,
        private val onEdit: (Expense) -> Unit,
        private val onDelete: (Expense) -> Unit
    ) :
        RecyclerView.ViewHolder(itemView) {

        private val title: TextView = itemView.findViewById(R.id.title)
        private val payer: TextView = itemView.findViewById(R.id.payer)
        private val originalAmount: TextView = itemView.findViewById(R.id.originalAmount)
        private val convertedAmount: TextView = itemView.findViewById(R.id.convertedAmount)
        private val personalCharges: TextView = itemView.findViewById(R.id.personalCharges)
        private val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(expense: Expense, currency: Currency) {
            title.text = expense.title
            val payerName = TripRepository.getParticipants()
                .find { it.id == expense.payerId }
                ?.name ?: "Desconhecido"
            payer.text = "Pago por $payerName"
            originalAmount.text = expense.total.format()
            if (expense.total.currency == currency) {
                convertedAmount.visibility = View.GONE
            } else {
                convertedAmount.visibility = View.VISIBLE
                val converted = Money(
                    amount = CurrencyConverter.convert(
                        expense.total.amount,
                        expense.total.currency,
                        currency
                    ),
                    currency = currency
                )
                convertedAmount.text = converted.format()
            }
            val personal = expense.personalCharges.joinToString { charge ->
                val participant = TripRepository.getParticipants()
                    .find { it.id == charge.participantId }
                    ?.name ?: "?"
                "${participant}: ${charge.money.format()}"
            }
            personalCharges.text = if (personal.isBlank()) {
                "Sem ajustes individuais"
            } else {
                "Ajustes individuais: $personal"
            }
            editButton.setOnClickListener { onEdit(expense) }
            deleteButton.setOnClickListener { onDelete(expense) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean =
            oldItem == newItem
    }
}
