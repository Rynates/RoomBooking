package com.example.roombooking.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.roombooking.data.DataRepository
import com.example.roombooking.prefs.AppSettings
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import timber.log.Timber

class DayCheckWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params),
    KoinComponent {
    private val repository: DataRepository by inject()

    override suspend fun doWork(): Result {
        return try {
            if (AppSettings.lastSyncTime != 0L) {

                val prevSyncDay =
                    Instant.ofEpochSecond(AppSettings.lastSyncTime).atZone(ZoneId.systemDefault())
                        .toOffsetDateTime()
                val currentDay = OffsetDateTime.now(ZoneId.systemDefault())

                Timber.d("PrevDay $prevSyncDay CurrentDay $currentDay")

                if (prevSyncDay.dayOfMonth != currentDay.dayOfMonth) {
                    AppSettings.roomsSelected?.forEach { room ->
                        room.id?.let {
                            repository.getMeetingsFromServerAsync(it)
                        }
                    }
                }
            } else {
                Timber.d("lastSyncTime = 0")
            }
            Result.success()
        } catch (throwable: Throwable) {
            Timber.e(throwable)
            Result.failure()
        }
    }
}
