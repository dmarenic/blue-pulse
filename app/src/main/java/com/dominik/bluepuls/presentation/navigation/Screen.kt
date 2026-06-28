package com.dominik.bluepuls.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.dominik.bluepuls.R

/**
 * Sve rute aplikacije na jednom mjestu (type-safe).
 * Napomena: ekran "Glasaj" više ne postoji - glasanje je unutar "Igrači".
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Matches : Screen("matches")
    data object Standings : Screen("standings")
    data object Players : Screen("players")
    data object Profile : Screen("profile")

    // Sekundarni ekrani (ne u bottom baru)
    data object MapScreen : Screen("map")
    data object Gallery : Screen("gallery")
    data object PlayerOfMonth : Screen("player_of_month")
    data object Details : Screen("details/{matchId}") {
        fun createRoute(matchId: String): String = "details/$matchId"
    }
    data object PlayerDetail : Screen("player/{playerId}") {
        fun createRoute(playerId: String): String = "player/$playerId"
    }
}

/** Stavka donje navigacijske trake. */
data class BottomNavItem(
    val screen: Screen,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, R.string.nav_home, Icons.Default.Home),
    BottomNavItem(Screen.Matches, R.string.nav_matches, Icons.AutoMirrored.Filled.List),
    BottomNavItem(Screen.Standings, R.string.nav_standings, Icons.Default.Leaderboard),
    BottomNavItem(Screen.Players, R.string.nav_stats, Icons.Default.Groups),
    BottomNavItem(Screen.Profile, R.string.nav_profile, Icons.Default.Person)
)
