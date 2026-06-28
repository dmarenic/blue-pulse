package com.dominik.bluepuls.data

import com.dominik.bluepuls.domain.ClubInfo
import com.dominik.bluepuls.domain.Match
import com.dominik.bluepuls.domain.Player
import com.dominik.bluepuls.domain.PlayerDetails
import com.dominik.bluepuls.domain.Standing
import com.dominik.bluepuls.domain.Team
import com.google.gson.annotations.SerializedName

/* =========================  IGRAČI  ========================= */

data class PlayersResponse(
    @SerializedName("player") val player: List<PlayerDto>? = emptyList()
)

data class PlayerDto(
    @SerializedName("idPlayer") val id: String?,
    @SerializedName("strPlayer") val name: String?,
    @SerializedName("strNumber") val number: String?,
    @SerializedName("strPosition") val position: String?,
    @SerializedName("strNationality") val nationality: String?,
    @SerializedName("dateBorn") val dateBorn: String?,
    @SerializedName("strStatus") val status: String?,
    @SerializedName("strCutout") val cutout: String?,
    @SerializedName("strThumb") val thumb: String?,
    @SerializedName("strRender") val render: String?,
    @SerializedName("idTeam") val idTeam: String? = null,
    @SerializedName("strTeam") val team: String? = null
)

/** Mapira DTO u domenski model; null ako nema osnovnih podataka. */
fun PlayerDto.toDomain(): Player? {
    val playerId = id ?: return null
    val playerName = name?.takeIf { it.isNotBlank() } ?: return null
    return Player(
        id = playerId,
        name = playerName,
        number = number?.toIntOrNull(),
        position = position?.takeIf { it.isNotBlank() } ?: "—",
        nationality = nationality?.takeIf { it.isNotBlank() },
        dateOfBirth = dateBorn?.takeIf { it.isNotBlank() && it != "0000-00-00" },
        photoUrl = listOfNotNull(cutout, thumb, render).firstOrNull { it.isNotBlank() },
        team = team?.takeIf { it.isNotBlank() }
    )
}

/* =========================  DETALJI IGRAČA  ========================= */

data class PlayerLookupResponse(
    @SerializedName("players") val players: List<PlayerDetailDto>? = emptyList()
)

data class PlayerDetailDto(
    @SerializedName("idPlayer") val id: String?,
    @SerializedName("strPlayer") val name: String?,
    @SerializedName("strNationality") val nationality: String?,
    @SerializedName("strPosition") val position: String?,
    @SerializedName("dateBorn") val dateBorn: String?,
    @SerializedName("strHeight") val height: String?,
    @SerializedName("strWeight") val weight: String?,
    @SerializedName("strDescriptionEN") val description: String?,
    @SerializedName("strThumb") val thumb: String?,
    @SerializedName("strCutout") val cutout: String?,
    @SerializedName("strTeam") val team: String?,
    @SerializedName("strNumber") val number: String?
)

fun PlayerDetailDto.toDomain(): PlayerDetails? {
    val pid = id ?: return null
    val pname = name?.takeIf { it.isNotBlank() } ?: return null
    return PlayerDetails(
        id = pid,
        name = pname,
        nationality = nationality?.takeIf { it.isNotBlank() },
        position = position?.takeIf { it.isNotBlank() },
        dateOfBirth = dateBorn?.takeIf { it.isNotBlank() && it != "0000-00-00" },
        height = height?.takeIf { it.isNotBlank() },
        weight = weight?.takeIf { it.isNotBlank() },
        description = description?.takeIf { it.isNotBlank() },
        photoUrl = listOfNotNull(cutout, thumb).firstOrNull { it.isNotBlank() },
        team = team?.takeIf { it.isNotBlank() },
        number = number?.toIntOrNull()
    )
}

/* =========================  KLUB  ========================= */

data class TeamResponse(
    @SerializedName("teams") val teams: List<TeamDto>? = emptyList()
)

data class TeamDto(
    @SerializedName("idTeam") val id: String?,
    @SerializedName("strTeam") val name: String?,
    @SerializedName("strBadge") val badge: String?,
    @SerializedName("strStadium") val stadium: String?,
    @SerializedName("strLocation") val location: String?,
    @SerializedName("intStadiumCapacity") val capacity: String?
)

fun TeamDto.toDomain(): ClubInfo = ClubInfo(
    name = name?.takeIf { it.isNotBlank() } ?: "Dinamo Zagreb",
    badgeUrl = badge?.takeIf { it.isNotBlank() },
    stadium = stadium?.takeIf { it.isNotBlank() },
    location = location?.takeIf { it.isNotBlank() },
    capacity = capacity?.takeIf { it.isNotBlank() }
)

/* =========================  UTAKMICE  ========================= */

// Next events koristi ključ "events", Last events koristi "results" -> oba nullable.
data class EventsResponse(
    @SerializedName("events") val events: List<EventDto>? = null,
    @SerializedName("results") val results: List<EventDto>? = null
) {
    fun list(): List<EventDto> = events ?: results ?: emptyList()
}

data class EventDto(
    @SerializedName("idEvent") val id: String?,
    @SerializedName("dateEvent") val date: String?,
    @SerializedName("strTime") val time: String?,
    @SerializedName("strLeague") val league: String?,
    @SerializedName("strHomeTeam") val homeTeam: String?,
    @SerializedName("strAwayTeam") val awayTeam: String?,
    @SerializedName("intHomeScore") val homeScore: String?,
    @SerializedName("intAwayScore") val awayScore: String?,
    @SerializedName("strHomeTeamBadge") val homeBadge: String?,
    @SerializedName("strAwayTeamBadge") val awayBadge: String?,
    @SerializedName("idHomeTeam") val idHomeTeam: String? = null,
    @SerializedName("idAwayTeam") val idAwayTeam: String? = null
)

fun EventDto.toDomain(): Match? {
    val eventId = id ?: return null
    return Match(
        id = eventId,
        homeTeam = Team(homeTeam ?: "?", homeBadge.orEmpty()),
        awayTeam = Team(awayTeam ?: "?", awayBadge.orEmpty()),
        date = date.orEmpty(),
        time = time?.takeIf { it.isNotBlank() && it != "00:00:00" },
        league = league.orEmpty(),
        homeGoals = homeScore?.toIntOrNull(),
        awayGoals = awayScore?.toIntOrNull()
    )
}

/* =========================  LJESTVICA  ========================= */

data class StandingsResponse(
    @SerializedName("table") val table: List<StandingDto>? = emptyList()
)

data class StandingDto(
    @SerializedName("intRank") val rank: String?,
    @SerializedName("idTeam") val teamId: String?,
    @SerializedName("strTeam") val teamName: String?,
    @SerializedName("strBadge") val badge: String?,
    @SerializedName("intPlayed") val played: String?,
    @SerializedName("intWin") val win: String?,
    @SerializedName("intDraw") val draw: String?,
    @SerializedName("intLoss") val loss: String?,
    @SerializedName("intGoalDifference") val goalDifference: String?,
    @SerializedName("intPoints") val points: String?,
    @SerializedName("strForm") val form: String?
)

fun StandingDto.toDomain(): Standing? {
    val r = rank?.toIntOrNull() ?: return null
    val name = teamName?.takeIf { it.isNotBlank() } ?: return null
    return Standing(
        rank = r,
        teamId = teamId.orEmpty(),
        teamName = name,
        badgeUrl = badge?.takeIf { it.isNotBlank() },
        played = played?.toIntOrNull() ?: 0,
        win = win?.toIntOrNull() ?: 0,
        draw = draw?.toIntOrNull() ?: 0,
        loss = loss?.toIntOrNull() ?: 0,
        goalDifference = goalDifference?.toIntOrNull() ?: 0,
        points = points?.toIntOrNull() ?: 0,
        form = form?.takeIf { it.isNotBlank() }
    )
}
