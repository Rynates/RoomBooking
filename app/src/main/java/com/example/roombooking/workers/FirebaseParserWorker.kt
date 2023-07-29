package com.example.roombooking.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.roombooking.data.DataRepository
import com.example.roombooking.network.FirebaseMessageParser
import com.example.roombooking.prefs.Constants
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class FirebaseParserWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params),
    KoinComponent {
    private val repository: DataRepository by inject()
    private val parser: FirebaseMessageParser by inject()

    override suspend fun doWork(): Result {
        return try {
            inputData.getString(Constants.KEY_JSON_FIREBASE_DATA)?.let { data ->
                parser.parse(data)?.let {
                    if (it.info.new.isNotEmpty()) {
                        repository.insertMeetings(*it.info.new.toTypedArray())
                    }
                    if (it.info.canceled.isNotEmpty()) {
                        repository.deleteMeetings(*it.info.canceled.toTypedArray())
                        Timber.e("Delete")
                    }
                }
            }
            Result.success()
        } catch (throwable: Throwable) {
            Timber.e(throwable)
            Result.failure()
        }
    }
}
