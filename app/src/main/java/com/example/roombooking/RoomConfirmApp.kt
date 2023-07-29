package com.example.roombooking

import android.app.Application
import com.chibatching.kotpref.Kotpref
import com.chibatching.kotpref.gsonpref.gson
import com.google.gson.Gson
import com.example.roombooking.di.appModule
import com.example.roombooking.di.serverModule
import com.jakewharton.threetenabp.AndroidThreeTen
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

open class RoomConfirmApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Kotpref.init(this)
        Kotpref.gson = Gson()
        AndroidThreeTen.init(this)
        initLogging()
        startKoin {
            androidLogger()
            androidContext(this@RoomConfirmApp)
            modules(listOf(appModule, serverModule))
        }
    }

    private fun initLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    super.log(priority, "RoomConfirm $tag", message, t)
                }
            })
        }
    }
}