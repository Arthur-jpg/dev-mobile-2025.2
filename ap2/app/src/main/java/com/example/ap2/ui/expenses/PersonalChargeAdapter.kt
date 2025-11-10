package com.example.ap2.ui.expenses

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ap2.data.TripRepository
import com.example.ap2.databinding.ItemPersonalChargeBinding
import com.example.ap2.model.PersonalCharge

class PersonalChargeAdapter(
    private val items: MutableList<PersonalCharge>,
    private val onRemove: (PersonalCharge) -> Unit
) : RecyclerView.Adapter<PersonalChargeAdapter.PersonalChargeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonalChargeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPersonalChargeBinding.inflate(inflater, parent, false)
        return PersonalChargeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PersonalChargeViewHolder, position: Int) {
        holder.bind(items[position], onRemove)
    }

    override fun getItemCount(): Int = items.size

    fun refresh() {
        notifyDataSetChanged()
    }

    class PersonalChargeViewHolder(
        private val binding: ItemPersonalChargeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(charge: PersonalCharge, onRemove: (PersonalCharge) -> Unit) = with(binding) {
            val name = TripRepository.getParticipants()
                .find { it.id == charge.participantId }
                ?.name ?: "?"
            title.text = "${charge.note ?: "Item individual"} • $name"
            amount.text = charge.money.format()
            removeButton.setOnClickListener { onRemove(charge) }
        }
    }
}
