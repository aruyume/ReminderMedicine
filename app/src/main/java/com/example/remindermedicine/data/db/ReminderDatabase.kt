package com.example.remindermedicine.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.remindermedicine.data.db.daos.Reminder
import com.example.remindermedicine.data.db.daos.ReminderDao

@Database(entities = [Reminder::class], version = 5)
abstract class ReminderDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: ReminderDatabase? = null

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val cursor = database.query("PRAGMA table_info(reminder);")
                var reminderTypeColumnExists = false
                while (cursor.moveToNext()) {
                    val columnName = cursor.getString(cursor.getColumnIndex("name"))
                    if (columnName == "reminderType") {
                        reminderTypeColumnExists = true
                        break
                    }
                }
                cursor.close()

                if (!reminderTypeColumnExists) {
                    database.execSQL("ALTER TABLE reminder ADD COLUMN reminderType TEXT NOT NULL DEFAULT ''")
                }
            }
        }

        fun getDatabase(context: Context): ReminderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReminderDatabase::class.java,
                    "reminder_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}