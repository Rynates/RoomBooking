package com.example.roombooking.data.entity

import androidx.room.Entity
import androidx.room.TypeConverters
import com.example.roombooking.db.DateConverter

@Entity(primaryKeys = ["id"], tableName = "meetings_table")
data class Meeting(
    val id: String,
    val roomId: Long,
    val members: String,
    val subject: String,
    val location: String,
    @TypeConverters(DateConverter::class)
    val startTime: String,
    @TypeConverters(DateConverter::class)
    val endTime: String,
    var confirmed: Boolean
)