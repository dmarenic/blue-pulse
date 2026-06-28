package com.dominik.bluepuls.domain

import com.dominik.bluepuls.core.VotingPeriod

/**
 * Pobjednik glasanja za igrača mjeseca (zapis u povijesti).
 */
data class PlayerOfMonth(
    val period: String,        // "2026-05"
    val year: Int,
    val month: Int,
    val playerId: String,
    val playerName: String,
    val playerPhotoUrl: String?,
    val votes: Int,
    val totalVotes: Int
) {
    val monthLabel: String get() = VotingPeriod.label(period)
}
