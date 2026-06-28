package com.dominik.bluepuls.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface MatchDao {

    @Query("SELECT * FROM matches")
    suspend fun getAll(): List<MatchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(matches: List<MatchEntity>)

    @Query("DELETE FROM matches")
    suspend fun clear()

    /** Atomarno zamijeni cijeli cache novim podacima. */
    @Transaction
    suspend fun replaceAll(matches: List<MatchEntity>) {
        clear()
        insertAll(matches)
    }
}
