package com.example.roombooking.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.roombooking.data.entity.Meeting
import com.example.roombooking.db.dao.MeetingDao

@Database(entities = [Meeting::class], version = 1, exportSchema = false)
abstract class MeetingsDatabase : RoomDatabase() {
    abstract fun meetingDao(): MeetingDao
}