package com.example.remindermedicine.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.remindermedicine.data.db.ReminderDatabase
import com.example.remindermedicine.data.db.daos.Reminder

class ReminderRepository(context: Context) {
    private val reminderDao = ReminderDatabase.getDatabase(context).reminderDao()

    fun getAllReminders(): LiveData<List<Reminder>> {
        return reminderDao.getAllReminders()
    }

    suspend fun insertReminder(reminder: Reminder) {
        reminderDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder)
    }
}