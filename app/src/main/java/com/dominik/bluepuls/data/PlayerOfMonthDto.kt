package com.dominik.bluepuls.data

import com.dominik.bluepuls.domain.PlayerOfMonth
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

/**
 * Firestore reprezentacija pobjednika mjeseca (kolekcija player_of_the_month_history).
 * Document ID = period ("2026-05") -> garantira jedan pobjednik po mjesecu.
 */
data class PlayerOfMonthDto(
    val period: String = "",
    val year: Long = 0,
    val month: Long = 0,
    val playerId: String = "",
    val playerName: String = "",
    val playerPhotoUrl: String = "",
    val votes: Long = 0,
    val totalVotes: Long = 0,
    @ServerTimestamp val createdAt: Timestamp? = null
)

fun PlayerOfMonthDto.toDomain(): PlayerOfMonth = PlayerOfMonth(
    period = period,
    year = year.toInt(),
    month = month.toInt(),
    playerId = playerId,
    playerName = playerName,
    playerPhotoUrl = playerPhotoUrl.ifBlank { null },
    votes = votes.toInt(),
    totalVotes = totalVotes.toInt()
)
