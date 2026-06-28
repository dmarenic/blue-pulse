package com.dominik.bluepuls.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dominik.bluepuls.presentation.MatchesViewModel
import com.dominik.bluepuls.presentation.navigation.Screen
import com.dominik.bluepuls.presentation.navigation.bottomNavItems
import com.dominik.bluepuls.ui.theme.SurfaceAccent
import com.dominik.bluepuls.ui.theme.SurfaceBlue01
import com.dominik.bluepuls.ui.theme.SurfaceBlue02
import com.dominik.bluepuls.ui.theme.SurfaceBlue03
import com.dominik.bluepuls.ui.theme.SurfaceWhiteA50
import com.dominik.bluepuls.ui.theme.TextWhite

@Composable
fun MainAppScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()

    // Zajednički MatchesViewModel za Matches + Details + Home (jedan dohvat).
    val sharedMatchesViewModel: MatchesViewModel =
        viewModel(factory = MatchesViewModel.Factory)

    Scaffold(
        containerColor = SurfaceBlue03,
        bottomBar = { AppBottomBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    matchesViewModel = sharedMatchesViewModel,
                    onOpenMap = { navController.navigate(Screen.MapScreen.route) },
                    onOpenGallery = { navController.navigate(Screen.Gallery.route) }
                )
            }

            composable(Screen.Matches.route) {
                MatchesScreen(navController = navController, viewModel = sharedMatchesViewModel)
            }

            // Objedinjeni ekran: igrači + glasanje
            composable(Screen.Standings.route) { StandingsScreen() }

            composable(Screen.Players.route) {
                PlayersScreen(
                    onOpenPlayerOfMonth = { navController.navigate(Screen.PlayerOfMonth.route) },
                    onPlayerClick = { id -> navController.navigate(Screen.PlayerDetail.createRoute(id)) }
                )
            }

            composable(Screen.PlayerOfMonth.route) {
                PlayerOfMonthScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.PlayerDetail.route,
                arguments = listOf(navArgument("playerId") { type = NavType.StringType })
            ) { backStackEntry ->
                PlayerDetailScreen(
                    playerId = backStackEntry.arguments?.getString("playerId").orEmpty(),
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) { ProfileScreen(onLogout = onLogout) }

            composable(Screen.MapScreen.route) {
                MapScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.Gallery.route) {
                GalleryScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.Details.route,
                arguments = listOf(navArgument("matchId") { type = NavType.StringType })
            ) { backStackEntry ->
                DetailsScreen(
                    matchId = backStackEntry.arguments?.getString("matchId"),
                    navController = navController,
                    viewModel = sharedMatchesViewModel
                )
            }
        }
    }
}

/**
 * Donja navigacija pričvršćena uz dno ekrana, preko cijele širine i skroz do
 * lijevog i desnog ruba (bez zaobljenja), s mekom sjenom, tankim rubom i Material 3
 * animiranim indikatorom aktivne stavke. Sigurnosni razmak (gesture/navigacijska
 * traka) rješava sam NavigationBar svojim ugrađenim insetom. Rute i ponašanje su
 * nepromijenjeni.
 */
@Composable
private fun AppBottomBar(navController: NavHostController) {
    val appear = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = appear,
        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it }
    ) {
        Surface(
            shape = RectangleShape,
            color = SurfaceBlue02,
            shadowElevation = 12.dp,
            border = BorderStroke(1.dp, SurfaceWhiteA50.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            NavigationBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = stringResource(item.labelRes)) },
                        label = { Text(stringResource(item.labelRes)) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SurfaceBlue01,
                            selectedTextColor = SurfaceAccent,
                            indicatorColor = SurfaceAccent,
                            unselectedIconColor = TextWhite,
                            unselectedTextColor = TextWhite
                        )
                    )
                }
            }
        }
    }
}
