package com.example.remindermedicine.di

import com.example.remindermedicine.data.repository.ReminderRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val repositoryModule: Module = module {
    factory {
        ReminderRepository(get())
    }
}