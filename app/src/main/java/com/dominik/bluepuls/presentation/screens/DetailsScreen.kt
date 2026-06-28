package com.dominik.bluepuls.presentation.screens

import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dominik.bluepuls.R
import com.dominik.bluepuls.core.MatchDateTime
import com.dominik.bluepuls.core.UiState
import com.dominik.bluepuls.domain.Match
import com.dominik.bluepuls.presentation.MatchesViewModel
import com.dominik.bluepuls.presentation.components.LoadingView
import com.dominik.bluepuls.presentation.components.rememberNotificationGate
import com.dominik.bluepuls.ui.theme.SurfaceAccent
import com.dominik.bluepuls.ui.theme.SurfaceBlue01
import com.dominik.bluepuls.ui.theme.SurfaceBlue02
import com.dominik.bluepuls.ui.theme.SurfaceBlue03
import com.dominik.bluepuls.ui.theme.SurfaceGold
import com.dominik.bluepuls.ui.theme.SurfaceWhite
import com.dominik.bluepuls.ui.theme.SurfaceWhiteA50

/**
 * Formatira TheSportsDB datum ("2026-07-02") + UTC vrijeme ("16:30:00")
 * u lokalno "02.07.2026. u 18:30". Zajednički helper za Matches/Details/Home.
 */
fun formatMatchDate(date: String, time: String?): String = MatchDateTime.formatted(date, time)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    matchId: String?,
    navController: NavController,
    viewModel: MatchesViewModel
) {
    // Reaktivno: deriviramo utakmicu iz repozitorijem-podržanog stanja po matchId-u.
    // Tako se ekran oporavi i nakon rotacije / povratka iz pozadine / process death,
    // kad ViewModel ponovno učita listu (init -> loadMatches), bez oslanjanja na
    // privremeno stanje koje je možda izgubljeno.
    val matchesState by viewModel.state.collectAsState()
    val match = (matchesState as? UiState.Success)?.data?.firstOrNull { it.id == matchId }
    val remindedIds by viewModel.remindedMatchIds.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val isReminderSet = match != null && remindedIds.contains(match.id)
    val onReminderClick = rememberNotificationGate {
        match?.let { viewModel.toggleReminder(it) }
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.match_details_title), color = SurfaceWhite) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = SurfaceWhite
                        )
                    }
                },
                actions = {
                    if (match != null) {
                        IconButton(onClick = { shareMatch(context, match) }) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.action_share), tint = SurfaceWhite)
                        }
                        IconButton(onClick = { addMatchToCalendar(context, match) }) {
                            Icon(Icons.Default.Event, contentDescription = stringResource(R.string.action_calendar), tint = SurfaceWhite)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceBlue03)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SurfaceBlue03
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SurfaceBlue03)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (match == null && matchesState is UiState.Loading) {
                LoadingView()
            } else if (match != null) {
                MatchDetailsCard(match)

                // Podsjetnik se nudi samo za nadolazeće (neodigrane) utakmice.
                if (!match.isPlayed) {
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onReminderClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isReminderSet) SurfaceBlue01 else SurfaceGold
                        ),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text(
                            text = if (isReminderSet) stringResource(R.string.details_cancel_reminder)
                                   else stringResource(R.string.details_set_reminder),
                            color = if (isReminderSet) SurfaceWhite else SurfaceBlue03,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Text(text = stringResource(R.string.match_not_found), color = SurfaceWhite)
            }
        }
    }
}

@Composable
private fun MatchDetailsCard(match: Match) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceBlue02)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = match.league.ifBlank { "REZULTAT / DETALJI" },
                color = SurfaceWhiteA50,
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.homeTeam.name,
                    color = SurfaceWhite,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
                Text(
                    text = if (match.isPlayed) " ${match.homeGoals} : ${match.awayGoals} " else " ${stringResource(R.string.match_vs)} ",
                    color = SurfaceAccent,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Text(
                    text = match.awayTeam.name,
                    color = SurfaceWhite,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )
            }

            if (!match.isPlayed) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.match_not_played), color = SurfaceWhiteA50, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = SurfaceWhiteA50.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Datum i vrijeme:", color = SurfaceWhiteA50, style = MaterialTheme.typography.bodySmall)
            Text(
                text = formatMatchDate(match.date, match.time),
                color = SurfaceWhite,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/** Dijeli utakmicu kao tekst (implicitni ACTION_SEND Intent). */
private fun shareMatch(context: android.content.Context, match: Match) {
    val result = if (match.isPlayed) "\nRezultat: ${match.homeGoals}:${match.awayGoals}" else ""
    val league = if (match.league.isNotBlank()) " (${match.league})" else ""
    val text = "${match.homeTeam.name} vs ${match.awayTeam.name} — " +
        "${formatMatchDate(match.date, match.time)}$league$result\n#BluePulse 💙"
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    runCatching { context.startActivity(Intent.createChooser(send, "Podijeli utakmicu")) }
}

/** Dodaje utakmicu u korisnikov kalendar (ACTION_INSERT na CalendarContract). */
private fun addMatchToCalendar(context: android.content.Context, match: Match) {
    val intent = Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, "Dinamo: ${match.homeTeam.name} vs ${match.awayTeam.name}")
        putExtra(CalendarContract.Events.EVENT_LOCATION, match.league)
        match.kickoffMillis()?.let { begin ->
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, begin)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, begin + 2 * 60 * 60 * 1000L)
        }
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Nema aplikacije za kalendar.", Toast.LENGTH_SHORT).show()
    }
}
