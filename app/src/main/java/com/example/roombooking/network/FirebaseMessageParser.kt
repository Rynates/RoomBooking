package com.example.roombooking.network

import android.content.Context
import com.google.gson.Gson
import com.example.roombooking.data.entity.MessageFCM
import timber.log.Timber

class FirebaseMessageParser(val context: Context) {

    fun parse(data: String): MessageFCM? {
        var message: MessageFCM? = null

        try {
            message = Gson().fromJson(data, MessageFCM::class.java)
        } catch (e: Exception) {
            Timber.e(e)
        }

        return message
    }
}