package com.example.remindermedicine.ui.fragment.reminder

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remindermedicine.data.db.daos.Reminder
import com.example.remindermedicine.data.repository.ReminderRepository
import kotlinx.coroutines.launch

class ReminderViewModel(private val reminderRepository: ReminderRepository) : ViewModel() {

    val reminders: LiveData<List<Reminder>> = reminderRepository.getAllReminders()

    fun addReminder(title: String, info: String, timestamp: Long, maxAmount: Int, reminderType: String) {
        val reminder = Reminder(
            title = title,
            info = info,
            timestamp = timestamp,
            amount = 0,
            maxAmount = maxAmount,
            reminderType = reminderType
        )
        viewModelScope.launch {
            reminderRepository.insertReminder(reminder)
        }
    }

    fun updateReminderAmount(reminder: Reminder, newAmount: Int) {
        val cappedAmount = newAmount.coerceIn(0, reminder.maxAmount)
        reminder.amount = cappedAmount
        viewModelScope.launch {
            reminderRepository.updateReminder(reminder)
        }
    }
}