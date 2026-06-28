package com.dominik.bluepuls.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.dominik.bluepuls.R
import com.dominik.bluepuls.core.UiState
import com.dominik.bluepuls.domain.Player
import com.dominik.bluepuls.presentation.PlayerSort
import com.dominik.bluepuls.presentation.PlayersViewModel
import com.dominik.bluepuls.presentation.components.EmptyView
import com.dominik.bluepuls.presentation.components.ErrorView
import com.dominik.bluepuls.presentation.components.LoadingView
import com.dominik.bluepuls.ui.theme.SurfaceAccent
import com.dominik.bluepuls.ui.theme.SurfaceBlue01
import com.dominik.bluepuls.ui.theme.SurfaceBlue02
import com.dominik.bluepuls.ui.theme.SurfaceBlue03
import com.dominik.bluepuls.ui.theme.SurfaceGold
import com.dominik.bluepuls.ui.theme.SurfaceWhite
import com.dominik.bluepuls.ui.theme.SurfaceWhiteA50

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayersScreen(
    onOpenPlayerOfMonth: () -> Unit,
    onPlayerClick: (String) -> Unit,
    viewModel: PlayersViewModel = viewModel(factory = PlayersViewModel.Factory)
) {
    val playersState by viewModel.players.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val sort by viewModel.sort.collectAsState()
    val votedPlayerId by viewModel.votedPlayerId.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.players_title), color = SurfaceWhite) },
                actions = {
                    IconButton(onClick = onOpenPlayerOfMonth) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = stringResource(R.string.pom_title), tint = SurfaceGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceBlue03)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SurfaceBlue03
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize().background(SurfaceBlue03)
        ) {
            SearchField(query = query, onQueryChange = viewModel::setQuery)
            SortRow(
                selected = sort,
                onSelect = viewModel::setSort,
                hasVoted = votedPlayerId != null,
                onRemoveVote = viewModel::removeVote
            )
            Box(modifier = Modifier.fillMaxSize()) {
                when (val s = playersState) {
                    is UiState.Loading -> LoadingView()
                    is UiState.Empty -> EmptyView()
                    is UiState.Error -> ErrorView(message = s.message, onRetry = viewModel::loadPlayers)
                    is UiState.Success -> PlayersList(
                        players = s.data,
                        votedPlayerId = votedPlayerId,
                        onVote = viewModel::vote,
                        onPlayerClick = onPlayerClick
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SurfaceWhiteA50) },
        placeholder = { Text(stringResource(R.string.players_search_hint), color = SurfaceWhiteA50) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = SurfaceWhite,
            unfocusedTextColor = SurfaceWhite,
            focusedBorderColor = SurfaceAccent,
            unfocusedBorderColor = SurfaceBlue01
        ),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortRow(
    selected: PlayerSort,
    onSelect: (PlayerSort) -> Unit,
    hasVoted: Boolean,
    onRemoveVote: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(R.string.players_sort_label), color = SurfaceWhiteA50, fontSize = 13.sp)
        SortChip(stringResource(R.string.sort_number), selected == PlayerSort.NUMBER) { onSelect(PlayerSort.NUMBER) }
        SortChip(stringResource(R.string.sort_surname), selected == PlayerSort.SURNAME) { onSelect(PlayerSort.SURNAME) }
        SortChip(stringResource(R.string.sort_votes), selected == PlayerSort.VOTES) { onSelect(PlayerSort.VOTES) }
        Spacer(Modifier.size(1.dp))
        if (hasVoted) {
            TextButton(onClick = onRemoveVote) {
                Text(stringResource(R.string.vote_reset), color = SurfaceAccent, fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 13.sp) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = SurfaceBlue02,
            labelColor = SurfaceWhite,
            selectedContainerColor = SurfaceAccent,
            selectedLabelColor = SurfaceBlue03
        )
    )
}

@Composable
private fun PlayersList(
    players: List<Player>,
    votedPlayerId: String?,
    onVote: (Player) -> Unit,
    onPlayerClick: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(players, key = { it.id }) { player ->
            PlayerCard(
                player = player,
                isVoted = player.id == votedPlayerId,
                onVote = { onVote(player) },
                onClick = { onPlayerClick(player.id) }
            )
        }
    }
}

@Composable
private fun PlayerCard(
    player: Player,
    isVoted: Boolean,
    onVote: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                if (isVoted) Modifier.border(2.dp, SurfaceAccent, MaterialTheme.shapes.medium)
                else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = SurfaceBlue02)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerAvatar(player)
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(player.name, color = SurfaceWhite, fontWeight = FontWeight.SemiBold)
                Text(
                    text = buildString {
                        append(player.position)
                        player.nationality?.let { append(" • $it") }
                    },
                    color = SurfaceWhiteA50,
                    fontSize = 12.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    player.number?.let {
                        Text(stringResource(R.string.player_number, it), color = SurfaceAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    player.age?.let {
                        Text(stringResource(R.string.player_age, it), color = SurfaceWhiteA50, fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.size(8.dp))
            VoteControl(isVoted = isVoted, onVote = onVote)
        }
    }
}

@Composable
private fun VoteControl(isVoted: Boolean, onVote: () -> Unit) {
    if (isVoted) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Check, contentDescription = null, tint = SurfaceAccent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(4.dp))
            Text(stringResource(R.string.vote_my_vote), color = SurfaceAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    } else {
        Button(
            onClick = onVote,
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceBlue01)
        ) {
            Text(stringResource(R.string.vote_action), color = SurfaceWhite, fontSize = 13.sp)
        }
    }
}

@Composable
private fun PlayerAvatar(player: Player) {
    Box(
        modifier = Modifier.size(52.dp).clip(CircleShape).background(SurfaceBlue01),
        contentAlignment = Alignment.Center
    ) {
        if (!player.photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = player.photoUrl,
                contentDescription = stringResource(R.string.cd_player_photo),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        } else {
            Text(
                text = player.number?.let { "#$it" } ?: player.name.take(1),
                color = SurfaceWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}
