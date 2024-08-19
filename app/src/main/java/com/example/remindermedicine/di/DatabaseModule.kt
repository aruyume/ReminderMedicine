package com.example.remindermedicine.di

import com.example.remindermedicine.data.db.ReminderDatabase
import org.koin.dsl.module

val databaseModule = module {

    single {
        ReminderDatabase.getDatabase(get())
    }

    single {
        get<ReminderDatabase>().reminderDao()
    }

}