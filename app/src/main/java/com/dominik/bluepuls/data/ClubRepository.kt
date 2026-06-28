package com.dominik.bluepuls.data

import com.dominik.bluepuls.core.Constants
import com.dominik.bluepuls.domain.ClubInfo

/**
 * Repozitorij za podatke o klubu (grb, stadion) s TheSportsDB-a.
 */
class ClubRepository(
    private val api: SportsDbApi
) {
    suspend fun getClubInfo(): Result<ClubInfo> {
        return try {
            val dto = api.getTeam(Constants.DINAMO_TEAM_ID).teams?.firstOrNull()
                ?: return Result.failure(IllegalStateException("Nema podataka o klubu."))
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
