package com.dominik.bluepuls.data

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit definicija TheSportsDB V1 API-ja.
 * Ključ je dio bazne putanje (vidi [RetrofitInstance]) pa ga ovdje ne ponavljamo.
 */
interface SportsDbApi {

    // Svi igrači momčadi.
    @GET("lookup_all_players.php")
    suspend fun getPlayers(@Query("id") teamId: String): PlayersResponse

    // Detalji jednog igrača.
    @GET("lookupplayer.php")
    suspend fun getPlayer(@Query("id") playerId: String): PlayerLookupResponse

    // Podaci o klubu (grb, stadion).
    @GET("lookupteam.php")
    suspend fun getTeam(@Query("id") teamId: String): TeamResponse

    // Nadolazeće utakmice.
    @GET("eventsnext.php")
    suspend fun getNextEvents(@Query("id") teamId: String): EventsResponse

    // Posljednje odigrane utakmice.
    @GET("eventslast.php")
    suspend fun getLastEvents(@Query("id") teamId: String): EventsResponse

    // Sve utakmice lige u sezoni (filtriramo Dinamove) - za maksimalan broj utakmica.
    @GET("eventsseason.php")
    suspend fun getSeasonEvents(
        @Query("id") leagueId: String,
        @Query("s") season: String
    ): EventsResponse

    // Ljestvica lige za sezonu.
    @GET("lookuptable.php")
    suspend fun getStandings(
        @Query("l") leagueId: String,
        @Query("s") season: String
    ): StandingsResponse
}
