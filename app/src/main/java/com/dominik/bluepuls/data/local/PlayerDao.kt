package com.dominik.bluepuls.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface PlayerDao {

    @Query("SELECT * FROM players")
    suspend fun getAll(): List<PlayerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(players: List<PlayerEntity>)

    @Query("DELETE FROM players")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(players: List<PlayerEntity>) {
        clear()
        insertAll(players)
    }
}
