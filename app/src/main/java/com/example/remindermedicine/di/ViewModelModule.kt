package com.example.remindermedicine.di

import com.example.remindermedicine.ui.fragment.reminder.ReminderViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val viewModelModule: Module = module {
    viewModel {
        ReminderViewModel(get())
    }
}