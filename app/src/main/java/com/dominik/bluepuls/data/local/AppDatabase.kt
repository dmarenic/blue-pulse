package com.dominik.bluepuls.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room baza za offline cache (utakmice + igrači).
 */
@Database(
    entities = [MatchEntity::class, PlayerEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
    abstract fun playerDao(): PlayerDao
}
