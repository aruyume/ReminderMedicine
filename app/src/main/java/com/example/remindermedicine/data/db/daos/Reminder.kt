package com.example.remindermedicine.data.db.daos

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val info: String,
    val timestamp: Long,
    var amount: Int,
    val maxAmount: Int,
    val reminderType: String
)