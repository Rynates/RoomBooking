package com.example.roombooking.flow.meetings

import com.example.roombooking.data.entity.Meeting
import com.example.roombooking.helpers.formatDate
import com.example.roombooking.helpers.toLocalDate
import com.example.roombooking.helpers.toMillis
import com.example.roombooking.prefs.AppSettings
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId


sealed class MeetingState {
    data class InProgress(val upcomingMeeting: Meeting?,
                          val currentMeeting: Meeting?,
                          val nextMeeting: Meeting?,
                          val msg: String,
                          val timeForConfirm: Long) : MeetingState()

    data class NoMeetings(val msg: String, val nextMeeting: Meeting?) : MeetingState()
}

fun calculateMeetingState(list: List<Meeting>?): MeetingState {
    var timeForConfirm: Long = -1

    val upcomingMeeting = findUpcomingMeeting(list)
    val currentMeeting: Meeting? = findCurrentMeeting(list)
    val nextMeeting = findNextMeeting(list)

    if (upcomingMeeting != null) {
        if (!upcomingMeeting.confirmed) {
            val startTime = upcomingMeeting.startTime.toLocalDate()?.plusSeconds(AppSettings.toSeconds)
            val confirmEndTime: Long = startTime?.toEpochSecond() ?: 0
            val nowTime = OffsetDateTime.now(ZoneId.systemDefault()).toEpochSecond()

            if (nowTime < confirmEndTime) {
                timeForConfirm = Duration.between(LocalDateTime.now(ZoneId.systemDefault()), startTime).toMinutes()
            }
        }
    }

    if (currentMeeting == null && upcomingMeeting == null) {
        val msg = if (nextMeeting != null) {
            "Available for ${duration(dateTo = nextMeeting.startTime)}"
        } else {
            "Available!"
        }
        return MeetingState.NoMeetings(msg, nextMeeting = nextMeeting)
    } else {
        var msg = ". . ."
        if (currentMeeting != null) {
            val meetingNowAndNext = findNextFreeTimeAfterMeeting(list, currentMeeting)
            msg = if (meetingNowAndNext != null) {
                "Available for ${duration(dateTo = nextMeeting!!.startTime)}"
            } else {
                "Available in ${duration(dateTo = currentMeeting.endTime)}"
            }

            val nextMeetingAfterCurrent = findNextMeeting(list, currentMeeting.endTime.toMillis())
            return MeetingState.InProgress(
                upcomingMeeting = upcomingMeeting,
                currentMeeting = currentMeeting,
                nextMeeting = nextMeetingAfterCurrent,
                timeForConfirm = timeForConfirm,
                msg = msg
            )
        } else {
            if (upcomingMeeting != null && upcomingMeeting.confirmed) {
                msg = "Next meeting is confirmed! \nStart in ${duration(dateTo = upcomingMeeting.startTime)}"
            }
            return MeetingState.InProgress(
                upcomingMeeting = upcomingMeeting,
                currentMeeting = currentMeeting,
                nextMeeting = nextMeeting,
                timeForConfirm = timeForConfirm,
                msg = msg
            )
        }
    }
}

private fun findCurrentMeeting(list: List<Meeting>?): Meeting? = list?.find {
    val startTime: Long = it.startTime.toMillis()
    val endTime: Long = it.endTime.toMillis()
    val nowTime: Long = OffsetDateTime.now(ZoneId.systemDefault()).toEpochSecond()
    nowTime in (startTime + 1) until endTime
}


private fun findUpcomingMeeting(list: List<Meeting>?): Meeting? = list?.find {
    val startTime: Long = it.startTime.toLocalDate()?.minusSeconds(AppSettings.fromSeconds)?.toEpochSecond() ?: 0
    val endTime: Long = it.endTime.toLocalDate()?.minusSeconds(AppSettings.fromSeconds)?.toEpochSecond() ?: 0
    val nowTime: Long = OffsetDateTime.now(ZoneId.systemDefault()).toEpochSecond()
    nowTime in (startTime + 1) until endTime
}

private fun findNextMeeting(list: List<Meeting>?, nowTime: Long = OffsetDateTime.now(ZoneId.systemDefault()).toEpochSecond()) =
    list?.find { nowTime <= it.startTime.toMillis() }

//Search next last meeting
private fun findNextFreeTimeAfterMeeting(list: List<Meeting>?, nowMeeting: Meeting): Pair<Meeting, Meeting?>? =
    list?.find { nowMeeting.endTime.toMillis() <= it.startTime.toMillis() }?.let { nextMeeting ->
        val timeTillNextMeeting = Duration.between(nowMeeting.endTime.toLocalDate(), nextMeeting.startTime.toLocalDate()).toMinutes()
        if (timeTillNextMeeting == 0L) {
            val isExistMoreMeetings = findNextMeeting(list, nextMeeting.endTime.toMillis())
            if (isExistMoreMeetings == null) {
                Pair(nextMeeting, null)
            } else {
                findNextFreeTimeAfterMeeting(list, nextMeeting)
            }
        } else {
            Pair(nowMeeting, nextMeeting)
        }
    }

private fun convertData(date: String) = formatDate(dateTime = date, formatTo = "HH:mm")

private fun duration(dateFrom: LocalDateTime = LocalDateTime.now(ZoneId.systemDefault()), dateTo: String): String {
    val timeFreeRoom = Duration.between(dateFrom, dateTo.toLocalDate()).toMinutes()
    return if (timeFreeRoom == 0L) {
        "< 1 min"
    } else {
        "$timeFreeRoom min"
    }
}
