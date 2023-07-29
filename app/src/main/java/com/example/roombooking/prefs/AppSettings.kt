package com.example.roombooking.prefs

import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.gsonpref.gsonNullablePref
import com.example.roombooking.data.entity.Room

object AppSettings : KotprefModel() {
    var roomsSelected: List<Room>? by gsonNullablePref(emptyList())
    var lastSyncTime: Long by longPref()
    var fromSeconds: Long by longPref(300L)
    var toSeconds: Long by longPref(300L)
}