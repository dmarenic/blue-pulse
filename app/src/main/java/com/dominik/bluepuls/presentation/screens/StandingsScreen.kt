package com.dominik.bluepuls.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.dominik.bluepuls.R
import com.dominik.bluepuls.core.Constants
import com.dominik.bluepuls.core.UiState
import com.dominik.bluepuls.domain.Standing
import com.dominik.bluepuls.presentation.StandingsViewModel
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
fun StandingsScreen(
    viewModel: StandingsViewModel = viewModel(factory = StandingsViewModel.Factory)
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.standings_title), color = SurfaceWhite) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceBlue03)
            )
        },
        containerColor = SurfaceBlue03
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize().background(SurfaceBlue03)) {
            when (val s = state) {
                is UiState.Loading -> LoadingView()
                is UiState.Empty -> EmptyView()
                is UiState.Error -> ErrorView(message = s.message, onRetry = viewModel::load)
                is UiState.Success -> StandingsTable(s.data)
            }
        }
    }
}

@Composable
private fun StandingsTable(rows: List<Standing>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { TableHeader() }
        items(rows, key = { it.teamId.ifBlank { it.rank.toString() } }) { row ->
            StandingRow(row, isDinamo = row.teamId == Constants.DINAMO_TEAM_ID)
        }
    }
}

@Composable
private fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceBlue02)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Cell("#", width = 28.dp, color = SurfaceWhiteA50)
        Text("Klub", color = SurfaceWhiteA50, fontSize = 12.sp, modifier = Modifier.weight(1f).padding(start = 8.dp))
        Cell("OU", width = 32.dp, color = SurfaceWhiteA50)
        Cell("GR", width = 36.dp, color = SurfaceWhiteA50)
        Cell("Bod", width = 40.dp, color = SurfaceWhiteA50)
    }
}

@Composable
private fun StandingRow(row: Standing, isDinamo: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDinamo) SurfaceBlue01 else SurfaceBlue03)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Cell(
            text = row.rank.toString(),
            width = 28.dp,
            color = if (row.rank <= 3) SurfaceAccent else SurfaceWhite,
            bold = true
        )
        if (!row.badgeUrl.isNullOrBlank()) {
            AsyncImage(model = row.badgeUrl, contentDescription = row.teamName, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
            Text(
                text = row.teamName,
                color = SurfaceWhite,
                fontSize = 14.sp,
                fontWeight = if (isDinamo) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            FormDots(row.form)
        }
        Cell(row.played.toString(), width = 32.dp, color = SurfaceWhiteA50)
        Cell(
            text = if (row.goalDifference > 0) "+${row.goalDifference}" else row.goalDifference.toString(),
            width = 36.dp,
            color = SurfaceWhiteA50
        )
        Cell(row.points.toString(), width = 40.dp, color = SurfaceAccent, bold = true)
    }
}

@Composable
private fun FormDots(form: String?) {
    if (form.isNullOrBlank()) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.padding(top = 3.dp)
    ) {
        form.takeLast(5).forEach { c ->
            val color = when (c.uppercaseChar()) {
                'W' -> SurfaceAccent
                'D' -> SurfaceGold
                'L' -> androidx.compose.ui.graphics.Color(0xFFE53935)
                else -> SurfaceWhiteA50
            }
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        }
    }
}

@Composable
private fun Cell(text: String, width: androidx.compose.ui.unit.Dp, color: androidx.compose.ui.graphics.Color, bold: Boolean = false) {
    Text(
        text = text,
        color = color,
        fontSize = 13.sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        textAlign = TextAlign.Center,
        modifier = Modifier.size(width = width, height = 20.dp)
    )
}
