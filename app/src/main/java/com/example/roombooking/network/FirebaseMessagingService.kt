package com.example.roombooking.network

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.roombooking.prefs.Constants
import com.example.roombooking.workers.FirebaseParserWorker
import timber.log.Timber

class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.d("From: ${remoteMessage.from}")
        remoteMessage.notification?.let {
            Timber.d("Message Notification Body: ${it.body}")
        }

        remoteMessage.data.isNotEmpty().let {
            Timber.d("Message data payload: %s", remoteMessage.data)
            val jsonData =
                workDataOf(Constants.KEY_JSON_FIREBASE_DATA to remoteMessage.data.toString())
            val parseWorkRequest = OneTimeWorkRequestBuilder<FirebaseParserWorker>()
                .setInputData(jsonData)
                .build()
            WorkManager.getInstance(applicationContext).enqueue(parseWorkRequest)
        }
    }

    override fun onNewToken(token: String) {
        Timber.d("NewToken: $token")
    }
}