package com.dominik.bluepuls.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.dominik.bluepuls.domain.PlayerDetails
import com.dominik.bluepuls.domain.PlayerOfMonth
import com.dominik.bluepuls.presentation.PlayerDetailViewModel
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
fun PlayerDetailScreen(
    playerId: String,
    onBack: () -> Unit,
    viewModel: PlayerDetailViewModel = viewModel(factory = PlayerDetailViewModel.factory(playerId))
) {
    val state by viewModel.state.collectAsState()
    val titles by viewModel.titles.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.player_detail_title), color = SurfaceWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back), tint = SurfaceWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceBlue03)
            )
        },
        containerColor = SurfaceBlue03
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding).fillMaxSize().background(SurfaceBlue03)) {
            when (val s = state) {
                is UiState.Loading -> LoadingView()
                is UiState.Empty -> ErrorView(message = stringResource(R.string.match_not_found), onRetry = viewModel::load)
                is UiState.Error -> ErrorView(message = s.message, onRetry = viewModel::load)
                is UiState.Success -> PlayerDetailContent(s.data, titles)
            }
        }
    }
}

@Composable
private fun PlayerDetailContent(player: PlayerDetails, titles: List<PlayerOfMonth>) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(140.dp).clip(CircleShape).background(SurfaceBlue02),
            contentAlignment = Alignment.Center
        ) {
            if (!player.photoUrl.isNullOrBlank()) {
                AsyncImage(model = player.photoUrl, contentDescription = player.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
            } else {
                Icon(Icons.Default.Person, contentDescription = null, tint = SurfaceAccent, modifier = Modifier.size(80.dp))
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(player.name, color = SurfaceWhite, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text(
            text = buildString {
                player.position?.let { append(it) }
                player.nationality?.let { append(if (isEmpty()) it else " • $it") }
                player.age?.let { append(" • $it god.") }
            },
            color = SurfaceWhiteA50,
            fontSize = 14.sp
        )
        player.number?.let {
            Text("#$it", color = SurfaceAccent, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }

        // Titule igrača mjeseca (zlatni pehar) ako ih ima.
        if (titles.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            TitlesCard(titles)
        }

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceBlue02)
        ) {
            Column(Modifier.padding(16.dp)) {
                player.height?.let { StatLine(stringResource(R.string.player_height), it) }
                player.weight?.let { StatLine(stringResource(R.string.player_weight), it) }
                player.team?.let { StatLine("Klub", it) }
            }
        }

        player.description?.let { desc ->
            Spacer(Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceBlue02)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.player_about), color = SurfaceAccent, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(desc, color = SurfaceWhiteA50, fontSize = 13.sp, lineHeight = 19.sp)
                }
            }
        }
    }
}

@Composable
private fun TitlesCard(titles: List<PlayerOfMonth>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceBlue02)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = SurfaceGold)
                Spacer(Modifier.size(8.dp))
                Text(
                    stringResource(R.string.pom_titles_count, titles.size),
                    color = SurfaceGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.player_titles_won) + ":", color = SurfaceWhiteA50, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            titles.forEach { title ->
                Text("🏆 ${title.monthLabel}", color = SurfaceWhite, fontSize = 13.sp, modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = SurfaceWhiteA50, fontSize = 14.sp)
        Text(value, color = SurfaceWhite, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}
