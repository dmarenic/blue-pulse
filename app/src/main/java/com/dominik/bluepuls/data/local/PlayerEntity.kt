package com.dominik.bluepuls.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dominik.bluepuls.domain.Player

/**
 * Room entitet za keširanog igrača (offline-first).
 * Sprema i klub (team) kako bi se cache mogao filtrirati na Dinamo Zagreb.
 */
@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val number: Int?,
    val position: String,
    val nationality: String?,
    val dateOfBirth: String?,
    val photoUrl: String?,
    val team: String?
)

fun Player.toEntity(): PlayerEntity = PlayerEntity(
    id = id,
    name = name,
    number = number,
    position = position,
    nationality = nationality,
    dateOfBirth = dateOfBirth,
    photoUrl = photoUrl,
    team = team
)

fun PlayerEntity.toDomain(): Player = Player(
    id = id,
    name = name,
    number = number,
    position = position,
    nationality = nationality,
    dateOfBirth = dateOfBirth,
    photoUrl = photoUrl,
    team = team
)
