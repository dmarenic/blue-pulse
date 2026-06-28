package com.dominik.bluepuls.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dominik.bluepuls.domain.Match
import com.dominik.bluepuls.domain.Team

/**
 * Room entitet za keširanu utakmicu (offline-first).
 */
@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val homeTeamName: String,
    val homeTeamLogo: String,
    val awayTeamName: String,
    val awayTeamLogo: String,
    val date: String,
    val time: String?,
    val league: String,
    val homeGoals: Int?,
    val awayGoals: Int?
)

fun Match.toEntity(): MatchEntity = MatchEntity(
    id = id,
    homeTeamName = homeTeam.name,
    homeTeamLogo = homeTeam.logo,
    awayTeamName = awayTeam.name,
    awayTeamLogo = awayTeam.logo,
    date = date,
    time = time,
    league = league,
    homeGoals = homeGoals,
    awayGoals = awayGoals
)

fun MatchEntity.toDomain(): Match = Match(
    id = id,
    homeTeam = Team(homeTeamName, homeTeamLogo),
    awayTeam = Team(awayTeamName, awayTeamLogo),
    date = date,
    time = time,
    league = league,
    homeGoals = homeGoals,
    awayGoals = awayGoals
)
