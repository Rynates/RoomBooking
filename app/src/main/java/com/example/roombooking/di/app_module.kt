package com.example.roombooking.di

import android.content.Context
import androidx.room.Room
import com.example.roombooking.data.DataRepository
import com.example.roombooking.data.DataRepositoryImpl
import com.example.roombooking.db.MeetingsDatabase
import com.example.roombooking.flow.meetings.MeetingsViewModel
import com.example.roombooking.flow.select_room.SelectRoomViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val appModule = module {
    single<DataRepository>(createdAtStart = true) {
        DataRepositoryImpl(
            get(),
            get(),
            get(),
            get()
        )
    }
    single { createMeetingsDatabase(get()) }
    viewModel { SelectRoomViewModel(get()) }
    viewModel { (id: Long) -> MeetingsViewModel(id, get()) }
}

fun createMeetingsDatabase(ctx: Context) =
    Room.databaseBuilder(ctx, MeetingsDatabase::class.java, "meetings_database")
        .fallbackToDestructiveMigration()
        .build()
