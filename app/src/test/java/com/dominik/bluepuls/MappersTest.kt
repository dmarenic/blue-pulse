package com.dominik.bluepuls

import com.dominik.bluepuls.data.EventDto
import com.dominik.bluepuls.data.PlayerDto
import com.dominik.bluepuls.data.UserDto
import com.dominik.bluepuls.data.toDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MappersTest {

    @Test
    fun playerDto_toDomain_mapsAllFields() {
        val dto = PlayerDto(
            id = "34", name = "Arijan Ademi", number = "5", position = "Midfielder",
            nationality = "North Macedonia", dateBorn = "1991-05-29", status = "Active",
            cutout = "https://img/cutout.png", thumb = "https://img/thumb.jpg", render = null
        )
        val player = dto.toDomain()
        assertEquals("34", player?.id)
        assertEquals("Arijan Ademi", player?.name)
        assertEquals(5, player?.number)
        assertEquals("North Macedonia", player?.nationality)
        assertEquals("Ademi", player?.lastName)
        // Prednost ima cutout (transparentna slika) pred thumb.
        assertEquals("https://img/cutout.png", player?.photoUrl)
    }

    @Test
    fun playerDto_toDomain_nullWhenMissingIdOrName() {
        val noId = PlayerDto("0".let { null }, "X", null, null, null, null, null, null, null, null)
        assertNull(noId.toDomain())
        val noName = PlayerDto("1", "", null, null, null, null, null, null, null, null)
        assertNull(noName.toDomain())
    }

    @Test
    fun eventDto_toDomain_playedMatchHasScores() {
        val dto = EventDto(
            id = "2273287", date = "2026-05-23", time = "16:45:00",
            league = "Croatian First Football League",
            homeTeam = "Dinamo Zagreb", awayTeam = "NK Lokomotiva",
            homeScore = "2", awayScore = "1", homeBadge = "h.png", awayBadge = "a.png"
        )
        val match = dto.toDomain()
        assertEquals("2273287", match?.id)
        assertEquals(2, match?.homeGoals)
        assertEquals(1, match?.awayGoals)
        assertTrue(match?.isPlayed == true)
    }

    @Test
    fun eventDto_toDomain_upcomingMatchHasNoScores() {
        val dto = EventDto(
            id = "1", date = "2026-07-02", time = "16:30:00", league = "Club Friendlies",
            homeTeam = "Brinje-Grosuplje", awayTeam = "Dinamo Zagreb",
            homeScore = null, awayScore = null, homeBadge = null, awayBadge = null
        )
        assertFalse(dto.toDomain()?.isPlayed == true)
    }

    @Test
    fun userDto_toDomain_mapsCountersAndFallsBackToDocId() {
        val dto = UserDto(
            uid = "", email = "navijac@dinamo.hr", favoritePlayer = "Ademi",
            votesCast = 1, photosUploaded = 2, createdAt = null
        )
        val profile = dto.toDomain("doc123")
        assertEquals("doc123", profile.uid)
        assertEquals(1, profile.votesCast)
        assertEquals(2, profile.photosUploaded)
        assertNull(profile.createdAtMillis)
    }
}
