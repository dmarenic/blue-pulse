package com.dominik.bluepuls.data

import com.dominik.bluepuls.domain.Match
import com.dominik.bluepuls.domain.Team

/**
 * Lokalni "seed" utakmica - fallback ako TheSportsDB nije dostupan,
 * pa ekran "Utakmice" nikad nije prazan.
 */
object MatchesDataSource {

    private const val DINAMO_BADGE =
        "https://r2.thesportsdb.com/images/media/team/badge/araidi1579955395.png"

    private val dinamo = Team("Dinamo Zagreb", DINAMO_BADGE)
    private const val HNL = "Croatian First Football League"

    val seed: List<Match> = listOf(
        // Odigrane
        Match("seed-m1", dinamo, Team("NK Lokomotiva", ""), "2026-05-23", "16:45:00", HNL, 0, 0),
        Match("seed-m2", Team("Hajduk Split", ""), dinamo, "2026-05-17", "19:00:00", HNL, 1, 2),
        // Nadolazeće
        Match("seed-m3", Team("Brinje-Grosuplje", ""), dinamo, "2026-07-02", "16:30:00", "Club Friendlies", null, null),
        Match("seed-m4", dinamo, Team("HNK Rijeka", ""), "2026-07-12", "19:00:00", HNL, null, null)
    )
}
