package com.example.roombooking.db

import androidx.room.TypeConverter
import com.example.roombooking.helpers.DATETIME_FORMAT_SERVER
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber
import java.util.*

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): String {
        var resultDate = ""
        try {
            value?.let {
                resultDate = DateTimeFormatter
                    .ofPattern(DATETIME_FORMAT_SERVER, Locale.ENGLISH)
                    .format(Instant.ofEpochMilli(value))
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        return resultDate
    }

    @TypeConverter
    fun dateToTimestamp(date: String?): Long {
        var resultDate: Long = 0
        try {
            val dateTimeFormatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT_SERVER, Locale.ENGLISH)
            resultDate = LocalDateTime.parse(date, dateTimeFormatter).atZone(ZoneId.systemDefault())
                .toEpochSecond()
        } catch (e: Exception) {
            Timber.e(e)
        }
        return resultDate
    }
}