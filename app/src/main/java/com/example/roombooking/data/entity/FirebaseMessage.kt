package com.example.roombooking.data.entity

data class MessageFCM(
    val info: FirebaseMessage
)

data class FirebaseMessage(
    val roomId: Long,
    val canceled: List<Meeting>,
    val new: List<Meeting>
)