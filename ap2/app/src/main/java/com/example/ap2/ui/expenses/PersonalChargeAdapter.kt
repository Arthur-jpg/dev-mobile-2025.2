package com.example.ap2.ui.expenses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ap2.data.TripRepository
import com.example.ap2.R
import com.example.ap2.model.PersonalCharge

class PersonalChargeAdapter(
    private val items: MutableList<PersonalCharge>,
    private val onRemove: (PersonalCharge) -> Unit
) : RecyclerView.Adapter<PersonalChargeAdapter.PersonalChargeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonalChargeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_personal_charge, parent, false)
        return PersonalChargeViewHolder(view)
    }

    override fun onBindViewHolder(holder: PersonalChargeViewHolder, position: Int) {
        holder.bind(items[position], onRemove)
    }

    override fun getItemCount(): Int = items.size

    fun refresh() {
        notifyDataSetChanged()
    }

    class PersonalChargeViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val title: TextView = itemView.findViewById(R.id.title)
        private val amount: TextView = itemView.findViewById(R.id.amount)
        private val removeButton: ImageButton = itemView.findViewById(R.id.removeButton)

        fun bind(charge: PersonalCharge, onRemove: (PersonalCharge) -> Unit) {
            val name = TripRepository.getParticipants()
                .find { it.id == charge.participantId }
                ?.name ?: "?"
            title.text = "${charge.note ?: "Item individual"} • $name"
            amount.text = charge.money.format()
            removeButton.setOnClickListener { onRemove(charge) }
        }
    }
}
