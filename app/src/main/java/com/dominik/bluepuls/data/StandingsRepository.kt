package com.dominik.bluepuls.data

import com.dominik.bluepuls.core.Constants
import com.dominik.bluepuls.domain.Standing

/**
 * Repozitorij za ljestvicu HNL-a (TheSportsDB lookuptable).
 */
class StandingsRepository(
    private val api: SportsDbApi
) {
    suspend fun getStandings(): Result<List<Standing>> {
        return try {
            val list = api.getStandings(Constants.HNL_LEAGUE_ID, Constants.CURRENT_SEASON)
                .table.orEmpty()
                .mapNotNull { it.toDomain() }
                .sortedBy { it.rank }
            if (list.isEmpty()) {
                Result.failure(IllegalStateException("Ljestvica trenutno nije dostupna."))
            } else {
                Result.success(list)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
