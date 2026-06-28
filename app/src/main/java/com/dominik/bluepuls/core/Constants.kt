package com.dominik.bluepuls.core

/**
 * Centralizirane konstante - nema "magic number" vrijednosti razbacanih po kodu.
 */
object Constants {

    // --- TheSportsDB ---
    // Bazni URL bez ključa; ključ se umeće iz BuildConfig u RetrofitInstance.
    const val SPORTSDB_BASE_URL = "https://www.thesportsdb.com/api/v1/json/"
    const val DINAMO_TEAM_ID = "133961"        // GNK Dinamo Zagreb na TheSportsDB
    const val HNL_LEAGUE_ID = "4629"           // Croatian First Football League
    const val CURRENT_SEASON = "2025-2026"     // za ljestvicu/utakmice (promjenjivo po sezoni)
    const val PREVIOUS_SEASON = "2024-2025"    // dodatni izvor odigranih utakmica
    const val NEXT_SEASON = "2026-2027"        // dodatni izvor nadolazećih utakmica

    // --- Stadion Maksimir (Google Maps) ---
    const val MAKSIMIR_LAT = 45.81888
    const val MAKSIMIR_LNG = 16.02030
    const val MAKSIMIR_NAME = "Stadion Maksimir"

    // --- Firestore kolekcije ---
    const val COLLECTION_USERS = "users"
    const val COLLECTION_PHOTOS = "photos"
    const val COLLECTION_VOTES = "votes"
    const val COLLECTION_POM_HISTORY = "player_of_the_month_history"

    // --- Senzor ---
    const val SHAKE_THRESHOLD_GRAVITY = 2.7f
}
