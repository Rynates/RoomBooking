package com.example.roombooking.flow.meetings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.roombooking.data.DataRepository
import com.example.roombooking.data.entity.ErrorType
import com.example.roombooking.data.entity.Meeting
import com.example.roombooking.helpers.Event
import com.example.roombooking.network.Resource
import java.util.*


class MeetingsViewModel(idRoom: Long, private val repository: DataRepository) : ViewModel() {
    private val _roomId = MutableLiveData<Long>()
    val apiResult: LiveData<Resource<List<Meeting>>>
    val apiRes: LiveData<List<Meeting>>
    val dbResult = repository.getMeetingsFromDbLiveData(idRoom)

    private val _meetingId = MutableLiveData<String>()
    val confirmResult: LiveData<Event<Resource<Unit>>>

    private val _toastText = MutableLiveData<Event<String>>()
    val toastText: LiveData<Event<String>> = _toastText

    private val _timer = Timer()

    private val _meetingInfo = MutableLiveData<MeetingState>()
    val meetingInfo: LiveData<MeetingState> = _meetingInfo

    init {
        apiResult = Transformations.switchMap(_roomId) {
            repository.getMeetingsFromServerLiveData(it)
        }
        apiRes = Transformations.switchMap(_roomId) {
            repository.getMeetingsFromDbLiveData(it)
        }

        confirmResult = Transformations.switchMap(_meetingId) { id ->
            repository.confirmMeeting(meetingId = id)
        }

        loadMeetings(idRoom)

        _timer.schedule(object : TimerTask() {
            override fun run() {
                calculateState(dbResult.value)
            }
        }, 0, 59000)

    }

    fun calculateState(list: List<Meeting>?) {
        _meetingInfo.postValue(calculateMeetingState(list))
    }

    fun confirmMeeting(id: String) {
        _meetingId.value = id
    }

    fun loadMeetings(id: Long) {
        _roomId.value = id
    }

    fun showToastMessage(message: String) {
        _toastText.value = Event(message)
    }

    fun showErrorMessage(error: ErrorType?) {
        error?.message?.let {
            _toastText.value = Event(it)
        }
    }

    override fun onCleared() {
        _timer.cancel()
        super.onCleared()
    }
}
