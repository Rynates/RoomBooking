package com.example.roombooking.network

import com.example.roombooking.data.entity.ErrorType

sealed class Resource<out T : Any> {
    data class Loading(val isShow: Boolean = true) : Resource<Nothing>()
    data class Success<out T : Any>(val data: T? = null) : Resource<T>()
    data class Error(val error: ErrorType) : Resource<Nothing>()
}