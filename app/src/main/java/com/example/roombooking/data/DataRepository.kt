package com.example.roombooking.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.roombooking.R
import com.example.roombooking.data.entity.ErrorType
import com.example.roombooking.data.entity.Meeting
import com.example.roombooking.data.entity.Room
import com.example.roombooking.db.MeetingsDatabase
import com.example.roombooking.helpers.Event
import com.example.roombooking.helpers.formatDate
import com.example.roombooking.helpers.getDistinct
import com.example.roombooking.network.ErrorParser
import com.example.roombooking.network.Resource
import com.example.roombooking.network.ServerApi
import com.example.roombooking.network.models.MeetingRequest
import com.example.roombooking.prefs.AppSettings
import kotlinx.coroutines.Dispatchers
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import timber.log.Timber


interface DataRepository {
    fun getRooms(): LiveData<Resource<List<Room>>>
    fun confirmMeeting(meetingId: String): LiveData<Event<Resource<Unit>>>
    fun validateSelectedRooms(list: List<Room>): LiveData<Event<Resource<Unit>>>
    fun getMeetingsFromDbLiveData(roomId: Long): LiveData<List<Meeting>>
    fun getMeetingsFromServerLiveData(roomId: Long): LiveData<Resource<List<Meeting>>>
    suspend fun getMeetingsFromServerAsync(roomId: Long): Resource<List<Meeting>>
    suspend fun settings()
    suspend fun insertMeetings(vararg meetings: Meeting)
    suspend fun deleteMeetings(vararg meetings: Meeting)
}

class DataRepositoryImpl(
    private val context: Context,
    private val api: ServerApi,
    private val errorParser: ErrorParser,
    database: MeetingsDatabase
) : DataRepository {

    private val meetingDao = database.meetingDao()

    override fun validateSelectedRooms(list: List<Room>) = liveData {
        list.filter { it.isChecked }.also {
            if (it.isNotEmpty()) {
                emit(Event(Resource.Success<Unit>()))
                AppSettings.roomsSelected = it
            } else {
                emit(Event(Resource.Error(ErrorType(message = context.getString(R.string.toast_rooms_no_selected)))))
            }
        }
    }

    override suspend fun settings() {
        try {
            val response = api.settings()
            if (response.isSuccessful) {
                response.body()?.let {
                    AppSettings.fromSeconds = it.fromSeconds
                    AppSettings.toSeconds = it.toSeconds
                }
            } else {
                Timber.e(response.errorBody().toString())
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun getRooms() = liveData(Dispatchers.IO) {
        emit(Resource.Loading(true))
        try {
            val response = api.fetchRooms()
            if (response.isSuccessful) {
                emit(Resource.Success(response.body().orEmpty()))
            } else {
                emit(
                    Resource.Error(
                        errorParser.handleServerError(
                            response.code(),
                            response.errorBody()
                        )
                    )
                )
            }
        } catch (e: Exception) {
            emit(wrapException(e))
        } finally {
            emit(Resource.Loading(false))
        }
    }

    override fun getMeetingsFromDbLiveData(roomId: Long) = meetingDao
        .getMeetingsByRoomId(longArrayOf(roomId))
        .getDistinct()

    override fun getMeetingsFromServerLiveData(roomId: Long) = liveData(Dispatchers.IO) {
        emit(Resource.Loading(true))
        settings()
        emit(getMeetingsFromServerAsync(roomId))
        emit(Resource.Loading(false))
    }

    override suspend fun getMeetingsFromServerAsync(roomId: Long): Resource<List<Meeting>> {
        var result: Resource<List<Meeting>> = Resource.Success()
        try {
            val syncDate = OffsetDateTime.now(ZoneId.systemDefault())
            val response = api.fetchMeetings(roomId = roomId, date = formatDate(syncDate))
            if (response.isSuccessful) {
                response.body().orEmpty().let {
                    meetingDao.updateAll(longArrayOf(roomId), *it.toTypedArray())
                    //TODO check and remove if update work on empty list
                    if (it.isEmpty()) {
                        result = Resource.Success(it)
                    }
                }
                AppSettings.lastSyncTime = syncDate.toEpochSecond()
            } else {
                result = Resource.Error(
                    errorParser.handleServerError(
                        response.code(),
                        response.errorBody()
                    )
                )
            }
        } catch (e: Exception) {
            result = wrapException(e)
        }

        return result
    }

    override suspend fun insertMeetings(vararg meetings: Meeting) {
        try {
            val result = meetingDao.insert(*meetings)
            Timber.d("Result of insert $result")
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override suspend fun deleteMeetings(vararg meetings: Meeting) {
        try {
            val result = meetingDao.deleteByMeetingId(meetings.map { it.id }.toTypedArray())
            Timber.d("Result of delete $result")
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun confirmMeeting(meetingId: String) = liveData(Dispatchers.IO) {
        emit(Event(Resource.Loading(true)))
        try {
            val response = api.putConfirmMeeting(MeetingRequest(meetingId = meetingId))
            if (response.isSuccessful) {
                emit(Event(Resource.Success<Unit>()))
            } else {
                emit(
                    Event(
                        Resource.Error(
                            errorParser.handleServerError(
                                response.code(),
                                response.errorBody()
                            )
                        )
                    )
                )
            }
        } catch (e: Exception) {
            emit(Event(wrapException(e)))
        } finally {
            emit(Event(Resource.Loading(false)))
        }
    }

    private fun wrapException(e: Exception): Resource.Error {
        Timber.e(e)
        return Resource.Error(
            ErrorType(
                message = errorParser.handleExceptionError(e)
            )
        )
    }
}