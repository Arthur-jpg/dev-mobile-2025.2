package com.example.ap2.model

import java.util.UUID

data class Participant(
    val id: String = UUID.randomUUID().toString(),
    val name: String
)

data class PersonalCharge(
    val id: String = UUID.randomUUID().toString(),
    val participantId: String,
    val money: Money,
    val note: String? = null
)

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val payerId: String,
    val total: Money,
    val sharedParticipantIds: List<String>,
    val personalCharges: List<PersonalCharge>
)

data class Transfer(
    val from: Participant,
    val to: Participant,
    val amount: Money
)
