package com.example.roombooking.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.roombooking.data.entity.Meeting

@Dao
interface MeetingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg meetings: Meeting): List<Long>

    @Delete
    suspend fun delete(vararg meetings: Meeting): Int

    @Transaction
    suspend fun updateAll(roomIds: LongArray, vararg meetings: Meeting) {
        deleteByRoomId(roomIds)
        insert(*meetings)
    }

    @Query("DELETE FROM meetings_table WHERE roomId IN (:ids)")
    suspend fun deleteByRoomId(ids: LongArray): Int

    @Query("DELETE FROM meetings_table WHERE id IN (:ids)")
    suspend fun deleteByMeetingId(ids: Array<String>): Int

    @Query("SELECT * FROM meetings_table WHERE roomId IN (:ids) ORDER BY startTime ASC")
    fun getMeetingsByRoomId(ids: LongArray): LiveData<List<Meeting>>
}