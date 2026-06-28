package com.dominik.bluepuls.domain

import com.dominik.bluepuls.core.MatchDateTime

/**
 * Čisti domenski modeli utakmice (neovisni o API-ju).
 */

data class Team(
    val name: String,
    val logo: String
)

data class Match(
    val id: String,
    val homeTeam: Team,
    val awayTeam: Team,
    val date: String,        // "2026-07-02"
    val time: String?,       // "16:30:00" (UTC, vidi MatchDateTime)
    val league: String,
    val homeGoals: Int?,
    val awayGoals: Int?
) {
    /** Odigrana = ima konačan rezultat (oba gola nisu null). */
    val isPlayed: Boolean get() = homeGoals != null && awayGoals != null

    /** Vrijeme početka u milisekundama (epoch), ili null ako se ne može parsirati. */
    fun kickoffMillis(): Long? = MatchDateTime.kickoffMillis(date, time)
}
