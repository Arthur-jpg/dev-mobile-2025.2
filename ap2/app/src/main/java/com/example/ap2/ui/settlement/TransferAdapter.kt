package com.example.ap2.ui.settlement

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ap2.R
import com.example.ap2.model.Currency
import com.example.ap2.model.Transfer

class TransferAdapter(
    private val items: MutableList<Transfer>
) : RecyclerView.Adapter<TransferAdapter.TransferViewHolder>() {

    var displayCurrency: Currency = Currency.BRL
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransferViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transfer, parent, false)
        return TransferViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransferViewHolder, position: Int) {
        holder.bind(items[position], displayCurrency)
    }

    override fun getItemCount(): Int = items.size

    fun replace(newItems: List<Transfer>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class TransferViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val primaryText: TextView = itemView.findViewById(R.id.primaryText)
        private val secondaryText: TextView = itemView.findViewById(R.id.secondaryText)

        fun bind(transfer: Transfer, displayCurrency: Currency) {
            val converted = transfer.amount.convertTo(displayCurrency)
            primaryText.text = "${transfer.from.name} → ${transfer.to.name}"
            secondaryText.text = converted.format()
        }
    }
}
