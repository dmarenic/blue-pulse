package com.dominik.bluepuls.presentation.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.dominik.bluepuls.domain.PlayerOfMonth
import com.dominik.bluepuls.domain.VoteCount
import com.dominik.bluepuls.presentation.CurrentResults
import com.dominik.bluepuls.presentation.PlayerOfMonthViewModel
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
fun PlayerOfMonthScreen(
    onBack: () -> Unit,
    viewModel: PlayerOfMonthViewModel = viewModel(factory = PlayerOfMonthViewModel.Factory)
) {
    val current by viewModel.current.collectAsState()
    val history by viewModel.history.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pom_title), color = SurfaceWhite) },
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
        if (isLoading) {
            Box(Modifier.padding(innerPadding).fillMaxSize()) { LoadingView() }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { InfoCard() }
            item { current?.let { CurrentResultsCard(it) } }
            item {
                Text(
                    stringResource(R.string.pom_history_title),
                    color = SurfaceWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            if (history.isEmpty()) {
                item { Text(stringResource(R.string.pom_history_empty), color = SurfaceWhiteA50, fontSize = 13.sp) }
            } else {
                items(history, key = { it.period }) { winner -> HistoryCard(winner) }
            }
        }
    }
}

@Composable
private fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceBlue02)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = SurfaceAccent)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.pom_info_title), color = SurfaceWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.size(12.dp))
            InfoBullet(stringResource(R.string.pom_info_what))
            InfoBullet(stringResource(R.string.pom_info_once))
            InfoBullet(stringResource(R.string.pom_info_change))
            InfoBullet(stringResource(R.string.pom_info_winner))
        }
    }
}

@Composable
private fun InfoBullet(text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SurfaceAccent, modifier = Modifier.size(16.dp).padding(top = 2.dp))
        Spacer(Modifier.size(8.dp))
        Text(text, color = SurfaceWhiteA50, fontSize = 13.sp)
    }
}

@Composable
private fun CurrentResultsCard(results: CurrentResults) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceBlue02)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("${stringResource(R.string.pom_current_title)} — ${results.periodLabel}", color = SurfaceAccent, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text(stringResource(R.string.pom_total_votes, results.totalVotes), color = SurfaceWhiteA50, fontSize = 12.sp)
            Spacer(Modifier.size(12.dp))

            if (results.entries.isEmpty()) {
                Text(stringResource(R.string.pom_no_votes_yet), color = SurfaceWhiteA50, fontSize = 13.sp)
            } else {
                results.entries.forEachIndexed { index, entry ->
                    ResultRow(rank = index + 1, entry = entry, total = results.totalVotes, isLeader = index == 0)
                    Spacer(Modifier.size(10.dp))
                }
            }
        }
    }
}

@Composable
private fun ResultRow(rank: Int, entry: VoteCount, total: Int, isLeader: Boolean) {
    val percent = if (total > 0) entry.count * 100 / total else 0
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isLeader) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = SurfaceGold, modifier = Modifier.size(20.dp))
            } else {
                Text("$rank.", color = SurfaceWhiteA50, fontWeight = FontWeight.Bold, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.size(10.dp))
            Text(
                entry.playerName,
                color = SurfaceWhite,
                fontWeight = if (isLeader) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            Text("${entry.count} • $percent%", color = if (isLeader) SurfaceGold else SurfaceAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.size(4.dp))
        LinearProgressIndicator(
            progress = { if (total > 0) entry.count.toFloat() / total else 0f },
            color = if (isLeader) SurfaceGold else SurfaceAccent,
            trackColor = SurfaceBlue01,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HistoryCard(winner: PlayerOfMonth) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceBlue02)
    ) {
        Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(SurfaceBlue01),
                contentAlignment = Alignment.Center
            ) {
                if (!winner.playerPhotoUrl.isNullOrBlank()) {
                    AsyncImage(model = winner.playerPhotoUrl, contentDescription = winner.playerName, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
                } else {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = SurfaceGold, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(winner.monthLabel, color = SurfaceWhiteA50, fontSize = 12.sp)
                Text(winner.playerName, color = SurfaceWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(stringResource(R.string.pom_votes_of_total, winner.votes, winner.totalVotes), color = SurfaceAccent, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = SurfaceGold)
                Text(stringResource(R.string.pom_badge), color = SurfaceGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
