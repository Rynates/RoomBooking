package com.example.roombooking.di

import com.example.roombooking.BuildConfig
import com.example.roombooking.network.ErrorParser
import com.example.roombooking.network.FirebaseMessageParser
import com.example.roombooking.network.ServerApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber


val serverModule = module {
    single { createOkHttpClient() }
    single { createWebService<ServerApi>(get(), BuildConfig.API_URL) }
    single { ErrorParser(get()) }
    single { FirebaseMessageParser(get()) }
}

fun createOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
    .addNetworkInterceptor(HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
        override fun log(message: String) {
            Timber.tag("OkHttp").d(message)
        }
    }).apply {
        level =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    })
    .build()

inline fun <reified T> createWebService(okHttpClient: OkHttpClient, url: String): T =
    Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()
        .create(T::class.java)