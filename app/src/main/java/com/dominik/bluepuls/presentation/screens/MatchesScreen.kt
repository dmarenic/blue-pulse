package com.dominik.bluepuls.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.dominik.bluepuls.R
import com.dominik.bluepuls.core.UiState
import com.dominik.bluepuls.domain.Match
import com.dominik.bluepuls.presentation.MatchFilter
import com.dominik.bluepuls.presentation.MatchesViewModel
import com.dominik.bluepuls.presentation.components.EmptyView
import com.dominik.bluepuls.presentation.components.ErrorView
import com.dominik.bluepuls.presentation.components.LoadingView
import com.dominik.bluepuls.presentation.navigation.Screen
import com.dominik.bluepuls.ui.theme.SurfaceAccent
import com.dominik.bluepuls.ui.theme.SurfaceBlue02
import com.dominik.bluepuls.ui.theme.SurfaceBlue03
import com.dominik.bluepuls.ui.theme.SurfaceWhite
import com.dominik.bluepuls.ui.theme.SurfaceWhiteA50

@Composable
fun MatchesScreen(
    navController: NavController,
    viewModel: MatchesViewModel
) {
    val state by viewModel.state.collectAsState()
    val filter by viewModel.filter.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(SurfaceBlue03)
    ) {
        FilterToggle(selected = filter, onSelect = viewModel::setFilter)

        Box(modifier = Modifier.fillMaxSize()) {
            when (val s = state) {
                is UiState.Loading -> LoadingView()
                is UiState.Empty -> EmptyView()
                is UiState.Error -> ErrorView(message = s.message, onRetry = viewModel::loadMatches)
                is UiState.Success -> {
                    val list = when (filter) {
                        // Nadolazeće: prve 4 po datumu (najbliže prema naprijed).
                        MatchFilter.UPCOMING -> s.data.filter { !it.isPlayed }.sortedBy { it.date }.take(4)
                        // Odigrane: zadnje 4 (najnovije prvo).
                        MatchFilter.PLAYED -> s.data.filter { it.isPlayed }.sortedByDescending { it.date }.take(4)
                    }
                    if (list.isEmpty()) {
                        EmptyView(message = stringResource(R.string.matches_empty_category))
                    } else {
                        MatchesList(
                            matches = list,
                            onMatchClick = { id -> navController.navigate(Screen.Details.createRoute(id)) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterToggle(selected: MatchFilter, onSelect: (MatchFilter) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ToggleChip(
            label = stringResource(R.string.matches_upcoming),
            selected = selected == MatchFilter.UPCOMING,
            modifier = Modifier.weight(1f)
        ) { onSelect(MatchFilter.UPCOMING) }
        ToggleChip(
            label = stringResource(R.string.matches_played),
            selected = selected == MatchFilter.PLAYED,
            modifier = Modifier.weight(1f)
        ) { onSelect(MatchFilter.PLAYED) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToggleChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                label,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = SurfaceBlue02,
            labelColor = SurfaceWhite,
            selectedContainerColor = SurfaceAccent,
            selectedLabelColor = SurfaceBlue03
        )
    )
}

@Composable
private fun MatchesList(matches: List<Match>, onMatchClick: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(matches, key = { it.id }) { match ->
            MatchCard(match = match, onClick = { onMatchClick(match.id) })
        }
    }
}

@Composable
private fun MatchCard(match: Match, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceBlue02)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TeamSide(name = match.homeTeam.name, badge = match.homeTeam.logo, alignEnd = true, modifier = Modifier.weight(1f))
                Text(
                    text = if (match.isPlayed) "${match.homeGoals} : ${match.awayGoals}" else stringResource(R.string.match_vs),
                    color = SurfaceAccent,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                TeamSide(name = match.awayTeam.name, badge = match.awayTeam.logo, alignEnd = false, modifier = Modifier.weight(1f))
            }
            Text(
                text = formatMatchDate(match.date, match.time),
                color = SurfaceWhiteA50,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
            )
            if (match.league.isNotBlank()) {
                Text(
                    text = match.league,
                    color = SurfaceWhite.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun TeamSide(name: String, badge: String, alignEnd: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start
    ) {
        if (!alignEnd && badge.isNotBlank()) {
            AsyncImage(model = badge, contentDescription = name, modifier = Modifier.size(28.dp))
        }
        Text(
            text = name,
            color = SurfaceWhite,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
            modifier = Modifier.padding(horizontal = 8.dp).weight(1f, fill = false)
        )
        if (alignEnd && badge.isNotBlank()) {
            AsyncImage(model = badge, contentDescription = name, modifier = Modifier.size(28.dp))
        }
    }
}
