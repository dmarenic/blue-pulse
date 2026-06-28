package com.dominik.bluepuls

import com.dominik.bluepuls.domain.Match
import com.dominik.bluepuls.domain.Team
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchTest {

    private fun match(home: Int?, away: Int?) = Match(
        id = "e1",
        homeTeam = Team("Dinamo Zagreb", ""),
        awayTeam = Team("Hajduk Split", ""),
        date = "2026-07-02",
        time = "19:00:00",
        league = "Croatian First Football League",
        homeGoals = home,
        awayGoals = away
    )

    @Test
    fun isPlayed_trueWhenBothScoresPresent() {
        assertTrue(match(2, 1).isPlayed)
        assertTrue(match(0, 0).isPlayed) // odigran 0-0
    }

    @Test
    fun isPlayed_falseWhenScoresMissing() {
        assertFalse(match(null, null).isPlayed)
        assertFalse(match(1, null).isPlayed)
    }
}
