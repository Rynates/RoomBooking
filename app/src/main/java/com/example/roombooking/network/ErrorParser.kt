package com.example.roombooking.network

import android.content.Context
import com.google.gson.Gson
import com.google.gson.stream.MalformedJsonException
import com.example.roombooking.R
import com.example.roombooking.data.entity.ErrorType
import okhttp3.ResponseBody
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException

class ErrorParser(val context: Context) {

    fun handleServerError(code: Int, responseBody: ResponseBody?): ErrorType {
        var serverError: ErrorType

        if (code == 500 && responseBody != null) {
            try {
                serverError = Gson().fromJson(responseBody.charStream(), ErrorType::class.java)
            } catch (e: Exception) {
                serverError = ErrorType()
                Timber.e(e)
            } finally {
                Timber.e(responseBody.string())
            }
        } else {
            serverError = ErrorType(
                status = code,
                message = "Error. Http code $code"
            )
        }
        return serverError
    }

    fun handleExceptionError(error: Throwable): String =
        when (error) {
            is SocketTimeoutException -> context.getString(R.string.requestTimeOutError)
            is MalformedJsonException -> context.getString(R.string.responseMalformedJson)
            is IOException -> context.getString(R.string.networkError)
            is HttpException -> error.response()!!.message()
            else -> context.getString(R.string.unknownError)
        }
}