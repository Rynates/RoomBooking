package com.example.roombooking.data.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Room(
    val id: Long? = null,
    val label: String? = null,
    val topic: String? = null,
    var isChecked: Boolean = false
) : Parcelable