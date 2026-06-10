/*
 * Copyright (c) 2026 Nishant Mishra
 *
 * This file is part of Tomato - a minimalist pomodoro timer for Android.
 *
 * Tomato is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Tomato is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Tomato.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `task` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `text` TEXT NOT NULL, `completed` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `completedAt` INTEGER, `sortOrder` INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `note` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `pinned` INTEGER NOT NULL, `sortOrder` INTEGER NOT NULL)")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `timer_session` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timerName` TEXT NOT NULL, `actualDuration` INTEGER NOT NULL, `startedAt` INTEGER NOT NULL, `date` TEXT NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `counter_record` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `sortOrder` INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `counter_entry` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `counterId` INTEGER NOT NULL, `date` TEXT NOT NULL, `count` INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `custom_timer` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `focusDuration` INTEGER NOT NULL, `shortBreakDuration` INTEGER NOT NULL, `longBreakDuration` INTEGER NOT NULL, `sessionLength` INTEGER NOT NULL, `alarmEnabled` INTEGER NOT NULL, `vibrateEnabled` INTEGER NOT NULL, `autoStartNext` INTEGER NOT NULL, `sortOrder` INTEGER NOT NULL)")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_counter_entry_counterId_date` ON `counter_entry` (`counterId`, `date`)")
    }
}

@Database(
    entities = [
        IntPreference::class, BooleanPreference::class, StringPreference::class, Stat::class,
        Task::class, Note::class, TimerSession::class,
        CounterRecord::class, CounterEntry::class, CustomTimer::class
    ],
    version = 4
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun preferenceDao(): PreferenceDao
    abstract fun statDao(): StatDao
    abstract fun systemDao(): SystemDao
    abstract fun taskDao(): TaskDao
    abstract fun noteDao(): NoteDao
    abstract fun timerSessionDao(): TimerSessionDao
    abstract fun counterRecordDao(): CounterRecordDao
    abstract fun customTimerDao(): CustomTimerDao
}