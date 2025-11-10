package com.example.ap2.ui.settlement

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ap2.databinding.ItemTransferBinding
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
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTransferBinding.inflate(inflater, parent, false)
        return TransferViewHolder(binding)
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
        private val binding: ItemTransferBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transfer: Transfer, displayCurrency: Currency) = with(binding) {
            val converted = transfer.amount.convertTo(displayCurrency)
            primaryText.text = "${transfer.from.name} → ${transfer.to.name}"
            secondaryText.text = converted.format()
        }
    }
}
