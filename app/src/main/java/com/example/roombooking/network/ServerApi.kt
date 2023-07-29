package com.example.roombooking.network

import com.example.roombooking.data.entity.Meeting
import com.example.roombooking.data.entity.Room
import com.example.roombooking.data.entity.Settings
import com.example.roombooking.network.models.MeetingRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path


interface ServerApi {
    @GET("settings")
    suspend fun settings(): Response<Settings>

    @GET("rooms")
    suspend fun fetchRooms(): Response<List<Room>>

    @GET("rooms/{roomId}/meetings/{date}")
    suspend fun fetchMeetings(
        @Path(value = "roomId") roomId: Long,
        @Path(value = "date") date: String
    ): Response<List<Meeting>>

    @PUT("rooms/meetings/confirm")
    suspend fun putConfirmMeeting(@Body meeting: MeetingRequest): Response<Unit>
}