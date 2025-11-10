package com.example.ap2.ui.participants

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ap2.databinding.ItemParticipantBinding
import com.example.ap2.model.Participant

class ParticipantAdapter(
    private val onRemove: (Participant) -> Unit
) : ListAdapter<Participant, ParticipantAdapter.ParticipantViewHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemParticipantBinding.inflate(inflater, parent, false)
        return ParticipantViewHolder(binding, onRemove)
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ParticipantViewHolder(
        private val binding: ItemParticipantBinding,
        private val onRemove: (Participant) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(participant: Participant) = with(binding) {
            participantName.text = participant.name
            removeButton.setOnClickListener { onRemove(participant) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<Participant>() {
        override fun areItemsTheSame(oldItem: Participant, newItem: Participant): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Participant, newItem: Participant): Boolean =
            oldItem == newItem
    }
}
