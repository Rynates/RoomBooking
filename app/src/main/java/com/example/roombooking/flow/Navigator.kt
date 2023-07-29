package com.example.roombooking.flow

interface Navigator {
    fun showRooms(withBack: Boolean = false)
    fun showMeetings()
}