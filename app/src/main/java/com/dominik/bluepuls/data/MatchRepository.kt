package com.dominik.bluepuls.data

import com.dominik.bluepuls.core.Constants
import com.dominik.bluepuls.core.DataOrigin
import com.dominik.bluepuls.core.Sourced
import com.dominik.bluepuls.data.local.MatchDao
import com.dominik.bluepuls.data.local.toDomain
import com.dominik.bluepuls.data.local.toEntity
import com.dominik.bluepuls.domain.Match

/**
 * Repozitorij za utakmice s offline-first fallbackom.
 *
 * Da bismo dohvatili ŠTO VIŠE utakmica koliko free TheSportsDB plan dopušta,
 * KOMBINIRAMO više endpointa (deduplicirano po ID-u):
 *   - eventsnext  (nadolazeće, već Dinamo)
 *   - eventslast  (odigrane, već Dinamo)
 *   - eventsseason (cijela liga, tekuća + prošla sezona) -> filtriramo Dinamo
 *
 * Fallback: API -> Room cache -> lokalni seed (ekran nikad prazan).
 */
class MatchRepository(
    private val api: SportsDbApi,
    private val matchDao: MatchDao
) {
    suspend fun getDinamoMatches(): Sourced<List<Match>> {
        // 1) API - kombiniraj sve izvore, dedupliciraj po ID-u (zadrži redoslijed).
        val merged = LinkedHashMap<String, Match>()

        runCatching { api.getNextEvents(Constants.DINAMO_TEAM_ID).list() }
            .getOrDefault(emptyList())
            .forEach { dto -> dto.toDomain()?.let { merged.putIfAbsent(it.id, it) } }

        runCatching { api.getLastEvents(Constants.DINAMO_TEAM_ID).list() }
            .getOrDefault(emptyList())
            .forEach { dto -> dto.toDomain()?.let { merged.putIfAbsent(it.id, it) } }

        for (season in listOf(Constants.NEXT_SEASON, Constants.CURRENT_SEASON, Constants.PREVIOUS_SEASON)) {
            runCatching { api.getSeasonEvents(Constants.HNL_LEAGUE_ID, season).list() }
                .getOrDefault(emptyList())
                .filter { it.idHomeTeam == Constants.DINAMO_TEAM_ID || it.idAwayTeam == Constants.DINAMO_TEAM_ID }
                .forEach { dto -> dto.toDomain()?.let { merged.putIfAbsent(it.id, it) } }
        }

        val matches = merged.values.toList()
        if (matches.isNotEmpty()) {
            runCatching { matchDao.replaceAll(matches.map { it.toEntity() }) }
            return Sourced(matches, DataOrigin.API)
        }

        // 2) Room cache
        val cached = runCatching { matchDao.getAll().map { it.toDomain() } }.getOrDefault(emptyList())
        if (cached.isNotEmpty()) {
            return Sourced(cached, DataOrigin.CACHE)
        }

        // 3) Lokalni seed
        return Sourced(MatchesDataSource.seed, DataOrigin.LOCAL)
    }
}
