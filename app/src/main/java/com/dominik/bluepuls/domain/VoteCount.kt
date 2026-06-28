package com.dominik.bluepuls.domain

/**
 * Broj glasova za jednog igrača (red globalne ljestvice glasanja).
 */
data class VoteCount(
    val playerId: String,
    val playerName: String,
    val count: Int
)
