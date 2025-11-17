package com.example.ap2.ui.participants

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ap2.R
import com.example.ap2.model.Participant

class ParticipantAdapter(
    private val onRemove: (Participant) -> Unit
) : RecyclerView.Adapter<ParticipantAdapter.ParticipantViewHolder>() {

    private val items = mutableListOf<Participant>()

    fun submitList(newItems: List<Participant>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    // chamdo quando precisa crair uma nova lista 
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_participant, parent, false)
        return ParticipantViewHolder(view, onRemove)
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ParticipantViewHolder(
        itemView: View,
        private val onRemove: (Participant) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val participantName: TextView = itemView.findViewById(R.id.participantName)
        private val removeButton: ImageButton = itemView.findViewById(R.id.removeButton)

        fun bind(participant: Participant) {
            participantName.text = participant.name
            removeButton.setOnClickListener { onRemove(participant) }
        }
    }
}
