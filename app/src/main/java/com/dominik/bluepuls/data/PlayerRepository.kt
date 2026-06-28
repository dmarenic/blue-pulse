package com.dominik.bluepuls.data

import android.util.Log
import com.dominik.bluepuls.core.Constants
import com.dominik.bluepuls.core.DataOrigin
import com.dominik.bluepuls.core.Sourced
import com.dominik.bluepuls.data.local.PlayerDao
import com.dominik.bluepuls.data.local.toDomain
import com.dominik.bluepuls.data.local.toEntity
import com.dominik.bluepuls.domain.Player
import com.dominik.bluepuls.domain.PlayerDetails

/**
 * Repozitorij za igrače s offline-first fallbackom:
 *   1. TheSportsDB API  -> uspjeh kešira u Room
 *   2. Room cache        -> zadnji uspješno dohvaćeni podaci
 *   3. Lokalni seed      -> ekran nikad nije prazan
 *
 * U svim slučajevima lista se nadopuni ručno dodanim igračima (firstTeamExtras),
 * jer ih besplatni API ne vraća.
 */
class PlayerRepository(
    private val api: SportsDbApi,
    private val playerDao: PlayerDao
) {
    suspend fun getDinamoPlayers(): Sourced<List<Player>> {
        // 1) API
        try {
            val players = api.getPlayers(Constants.DINAMO_TEAM_ID)
                .player.orEmpty()
                // ISKLJUČIVO igrači GNK Dinamo Zagreb (filtriramo tuđe klubove) + bez umirovljenih.
                .filter { dto ->
                    dto.idTeam == Constants.DINAMO_TEAM_ID &&
                        (dto.team == null || dto.team.equals("Dinamo Zagreb", ignoreCase = true)) &&
                        !dto.status.equals("Retired", ignoreCase = true) &&
                        dto.id !in PlayersDataSource.excludedPlayerIds
                }
                .mapNotNull { it.toDomain() }

            if (players.isNotEmpty()) {
                runCatching { playerDao.replaceAll(players.map { it.toEntity() }) }
                return Sourced(players.withExtras(), DataOrigin.API)
            }
        } catch (e: Exception) {
            Log.w(TAG, "API igrači nedostupni, idem na Room cache: ${e.message}")
        }

        // 2) Room cache - i ovdje filtriramo na Dinamo Zagreb (obrana od zastarjelih zapisa).
        val cached = runCatching { playerDao.getAll().map { it.toDomain() } }.getOrDefault(emptyList())
            .filter { it.isDinamoZagreb() && it.id !in PlayersDataSource.excludedPlayerIds }
        if (cached.isNotEmpty()) {
            return Sourced(cached.withExtras(), DataOrigin.CACHE)
        }

        // 3) Lokalni seed (već sadrži ručno dodane igrače)
        return Sourced(PlayersDataSource.dinamoSquad, DataOrigin.LOCAL)
    }

    /** Igrač pripada Dinamu ako klub nije postavljen (seed/extras) ili je "Dinamo Zagreb". */
    private fun Player.isDinamoZagreb(): Boolean =
        team == null || team.equals("Dinamo Zagreb", ignoreCase = true)

    /** Detalji jednog igrača (TheSportsDB lookupplayer). */
    suspend fun getPlayerDetails(playerId: String): Result<PlayerDetails> {
        return try {
            val details = api.getPlayer(playerId).players?.firstOrNull()?.toDomain()
                ?: return Result.failure(IllegalStateException("Detalji igrača nisu dostupni."))
            Result.success(details)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Nadopuni listu ručno dodanim igračima koji nisu već prisutni (po prezimenu). */
    private fun List<Player>.withExtras(): List<Player> {
        val existingSurnames = mapTo(HashSet()) { it.lastName.lowercase() }
        val extras = PlayersDataSource.firstTeamExtras
            .filter { it.lastName.lowercase() !in existingSurnames }
        return this + extras
    }

    companion object {
        private const val TAG = "PlayerRepository"
    }
}
