package com.dominik.bluepuls.presentation.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.Stadium
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import com.dominik.bluepuls.R
import com.dominik.bluepuls.core.UiState
import com.dominik.bluepuls.domain.ClubInfo
import com.dominik.bluepuls.domain.Match
import com.dominik.bluepuls.presentation.HomeViewModel
import com.dominik.bluepuls.presentation.MatchesViewModel
import com.dominik.bluepuls.presentation.components.ShakeDetectorEffect
import com.dominik.bluepuls.presentation.components.rememberNotificationGate
import com.dominik.bluepuls.ui.theme.SurfaceAccent
import com.dominik.bluepuls.ui.theme.SurfaceBlue01
import com.dominik.bluepuls.ui.theme.SurfaceBlue02
import com.dominik.bluepuls.ui.theme.SurfaceBlue03
import com.dominik.bluepuls.ui.theme.SurfaceGold
import com.dominik.bluepuls.ui.theme.SurfaceWhite
import com.dominik.bluepuls.ui.theme.SurfaceWhiteA50
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

// 8pt razmaci kroz cijeli ekran.
private val Gap = 8.dp
private val GapL = 16.dp
private val GapXL = 24.dp

@Composable
fun HomeScreen(
    matchesViewModel: MatchesViewModel,
    onOpenMap: () -> Unit,
    onOpenGallery: () -> Unit,
    homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val state by matchesViewModel.state.collectAsState()
    val clubInfo by homeViewModel.clubInfo.collectAsState()
    val remindedIds by matchesViewModel.remindedMatchIds.collectAsState()
    val message by matchesViewModel.message.collectAsState()
    val nextMatch = (state as? UiState.Success)?.data?.let { findNextMatch(it) }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val refreshingText = stringResource(R.string.shake_refreshing)

    // SENZOR: protresanje uređaja osvježava podatke (uz haptiku i toast).
    ShakeDetectorEffect {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        Toast.makeText(context, refreshingText, Toast.LENGTH_SHORT).show()
        matchesViewModel.loadMatches()
    }

    // NOTIFIKACIJE: podsjetnik za sljedeću utakmicu (stvarno vrijeme, WorkManager).
    val onReminderClick = rememberNotificationGate {
        nextMatch?.let { matchesViewModel.toggleReminder(it) }
    }
    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            matchesViewModel.clearMessage()
        }
    }
    val isNextReminderSet = nextMatch != null && remindedIds.contains(nextMatch.id)
    val canRemind = nextMatch?.isPlayed == false

    // Ulazna animacija – pokreće se jednom pri prvom prikazu.
    val appear = remember { MutableTransitionState(false).apply { targetState = true } }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(homeBackgroundBrush())
    ) {
        val contentWidth = if (maxWidth > 640.dp) 600.dp else maxWidth
        val landscape = maxHeight < maxWidth

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = contentWidth)
                    .fillMaxWidth()
                    .padding(horizontal = GapL, vertical = GapL)
            ) {
                AnimatedVisibility(
                    visibleState = appear,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 8 }
                ) {
                    HeroSection(club = clubInfo, match = nextMatch, compact = landscape)
                }

                Spacer(Modifier.height(GapXL))

                AnimatedVisibility(
                    visibleState = appear,
                    enter = fadeIn(tween(500, delayMillis = 120)) +
                        slideInVertically(tween(500, delayMillis = 120)) { it / 6 }
                ) {
                    NextMatchCard(match = nextMatch, club = clubInfo)
                }

                if (canRemind) {
                    Spacer(Modifier.height(GapL))
                    AnimatedVisibility(
                        visibleState = appear,
                        enter = fadeIn(tween(450, delayMillis = 240)) +
                            scaleIn(tween(450, delayMillis = 240), initialScale = 0.92f)
                    ) {
                        PrimaryReminderButton(isSet = isNextReminderSet, onClick = onReminderClick)
                    }
                }

                Spacer(Modifier.height(GapXL))

                AnimatedVisibility(
                    visibleState = appear,
                    enter = fadeIn(tween(450, delayMillis = 320)) +
                        slideInVertically(tween(450, delayMillis = 320)) { it / 5 }
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(GapL)) {
                        SecondaryAction(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Place,
                            label = stringResource(R.string.home_action_stadium),
                            onClick = onOpenMap
                        )
                        SecondaryAction(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.PhotoLibrary,
                            label = stringResource(R.string.home_action_gallery),
                            onClick = onOpenGallery
                        )
                    }
                }

                Spacer(Modifier.height(GapL))
            }
        }
    }
}

/* ========================  POZADINA  ======================== */

/** Slojevita premium pozadina (samo postojeće boje + prozirnost). */
@Composable
private fun homeBackgroundBrush(): Brush = Brush.verticalGradient(
    0f to SurfaceBlue02,
    0.35f to SurfaceBlue03,
    1f to SurfaceBlue03
)

/* ========================  HERO  ======================== */

@Composable
private fun HeroSection(club: ClubInfo?, match: Match?, compact: Boolean) {
    val (greetingText, greetingIcon) = greeting(match)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(listOf(SurfaceBlue01, SurfaceBlue02, SurfaceBlue03))
            )
            .border(1.dp, SurfaceWhiteA50.copy(alpha = 0.16f), RoundedCornerShape(28.dp))
    ) {
        // Zamućeni grb kluba kao suptilna "stadionska" pozadina (degradira na starijim API-jima).
        if (!club?.badgeUrl.isNullOrBlank()) {
            AsyncImage(
                model = club!!.badgeUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(if (compact) 180.dp else 240.dp)
                    .blur(28.dp)
                    .alpha(0.10f)
            )
        }
        // Gradient preko slike radi čitljivosti teksta.
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.horizontalGradient(listOf(SurfaceBlue03.copy(alpha = 0.85f), SurfaceBlue03.copy(alpha = 0.15f)))
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GapXL, vertical = if (compact) GapL else GapXL),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(greetingIcon, contentDescription = null, tint = SurfaceGold, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(Gap))
                    Text(
                        text = greetingText,
                        color = SurfaceWhiteA50,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(Modifier.height(Gap))
                Text(
                    text = club?.name ?: "Dinamo Zagreb",
                    color = SurfaceWhite,
                    fontSize = if (compact) 26.sp else 32.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(Gap / 2))
                Text(
                    text = stringResource(R.string.home_slogan),
                    color = SurfaceAccent,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.padding(top = Gap),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Vibration, contentDescription = null, tint = SurfaceWhiteA50, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.size(Gap / 2))
                    Text(
                        text = stringResource(R.string.home_shake_hint),
                        color = SurfaceWhiteA50,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (!club?.badgeUrl.isNullOrBlank()) {
                Spacer(Modifier.size(GapL))
                Box(
                    modifier = Modifier
                        .size(if (compact) 64.dp else 84.dp)
                        .clip(CircleShape)
                        .background(SurfaceBlue03.copy(alpha = 0.55f))
                        .border(1.dp, SurfaceWhiteA50.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = club!!.badgeUrl,
                        contentDescription = club.name,
                        modifier = Modifier.size(if (compact) 46.dp else 60.dp)
                    )
                }
            }
        }
    }
}

/* ========================  NEXT MATCH CARD  ======================== */

@Composable
private fun NextMatchCard(match: Match?, club: ClubInfo?) {
    PremiumCard(borderColor = SurfaceAccent.copy(alpha = 0.35f)) {
        Column(modifier = Modifier.padding(GapXL)) {
            // Zaglavlje: oznaka + status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (match?.isPlayed == false) stringResource(R.string.home_next_match)
                    else stringResource(R.string.home_last_match),
                    color = SurfaceWhiteA50,
                    style = MaterialTheme.typography.labelSmall
                )
                if (match != null) MatchStatusBadge(match)
            }

            if (match == null) {
                Spacer(Modifier.height(GapL))
                Text(stringResource(R.string.home_no_matches), color = SurfaceWhiteA50)
                return@Column
            }

            Spacer(Modifier.height(GapL))

            // Natjecanje
            if (match.league.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.SportsSoccer, contentDescription = null, tint = SurfaceWhiteA50, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(Gap / 2))
                    Text(match.league, color = SurfaceWhiteA50, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(GapL))
            }

            // Momčadi (centrirano) + rezultat ili VS
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeamColumn(name = match.homeTeam.name, badge = match.homeTeam.logo, modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = Gap)) {
                    Text(
                        text = if (match.isPlayed) "${match.homeGoals} : ${match.awayGoals}" else stringResource(R.string.match_vs),
                        color = SurfaceAccent,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    )
                }
                TeamColumn(name = match.awayTeam.name, badge = match.awayTeam.logo, modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(GapL))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SurfaceWhiteA50.copy(alpha = 0.12f))
            )
            Spacer(Modifier.height(GapL))

            // Datum + stadion
            InfoLine(icon = Icons.Rounded.CalendarMonth, text = formatMatchDate(match.date, match.time))
            val venue = stadiumFor(match, club)
            if (venue != null) {
                Spacer(Modifier.height(Gap))
                InfoLine(icon = Icons.Rounded.Stadium, text = venue)
            }

            // Odbrojavanje (samo za nadolazeće)
            if (!match.isPlayed) {
                match.kickoffMillis()?.let { kickoff ->
                    Spacer(Modifier.height(GapL))
                    CountdownRow(kickoffMillis = kickoff)
                }
            }
        }
    }
}

@Composable
private fun TeamColumn(name: String, badge: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(SurfaceBlue03.copy(alpha = 0.6f))
                .border(1.dp, SurfaceWhiteA50.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (badge.isNotBlank()) {
                AsyncImage(model = badge, contentDescription = name, modifier = Modifier.size(40.dp))
            } else {
                Icon(Icons.Rounded.SportsSoccer, contentDescription = null, tint = SurfaceWhiteA50, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(Modifier.height(Gap))
        Text(
            text = name,
            color = SurfaceWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun InfoLine(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = SurfaceAccent, modifier = Modifier.size(16.dp))
        Spacer(Modifier.size(Gap))
        Text(text, color = SurfaceWhite, style = MaterialTheme.typography.bodyMedium)
    }
}

/* ========================  STATUS BADGE  ======================== */

private enum class MatchStatus { LIVE, TODAY, UPCOMING, FINAL }

@Composable
private fun MatchStatusBadge(match: Match) {
    val now = rememberTickingNow(30_000L)
    val status = statusOf(match, now)
    val (labelRes, bg, fg) = when (status) {
        MatchStatus.LIVE -> Triple(R.string.home_status_live, SurfaceAccent, SurfaceBlue03)
        MatchStatus.TODAY -> Triple(R.string.home_status_today, SurfaceGold, SurfaceBlue03)
        MatchStatus.UPCOMING -> Triple(R.string.home_status_upcoming, SurfaceBlue01, SurfaceWhite)
        MatchStatus.FINAL -> Triple(R.string.home_status_final, SurfaceBlue03.copy(alpha = 0.6f), SurfaceWhiteA50)
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, SurfaceWhiteA50.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (status == MatchStatus.LIVE) {
            val pulse = rememberInfiniteTransition(label = "live")
            val a by pulse.animateFloat(
                initialValue = 0.3f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "liveDot"
            )
            Icon(Icons.Rounded.Circle, contentDescription = null, tint = fg.copy(alpha = a), modifier = Modifier.size(8.dp))
            Spacer(Modifier.size(Gap / 2))
        }
        Text(stringResource(labelRes), color = fg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}

/* ========================  COUNTDOWN  ======================== */

@Composable
private fun CountdownRow(kickoffMillis: Long) {
    val now = rememberTickingNow(30_000L)
    val remaining = kickoffMillis - now
    if (remaining <= 0) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Schedule, contentDescription = null, tint = SurfaceAccent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(Gap))
            Text(stringResource(R.string.home_countdown_starting), color = SurfaceWhite, fontWeight = FontWeight.Bold)
        }
        return
    }
    val days = (remaining / 86_400_000).toInt()
    val hours = ((remaining / 3_600_000) % 24).toInt()
    val minutes = ((remaining / 60_000) % 60).toInt()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Gap),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CountdownBlock(days, stringResource(R.string.home_cd_days), Modifier.weight(1f))
        CountdownBlock(hours, stringResource(R.string.home_cd_hours), Modifier.weight(1f))
        CountdownBlock(minutes, stringResource(R.string.home_cd_mins), Modifier.weight(1f))
    }
}

@Composable
private fun CountdownBlock(value: Int, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceBlue03.copy(alpha = 0.55f))
            .border(1.dp, SurfaceWhiteA50.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .padding(vertical = GapL),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value.toString().padStart(2, '0'), color = SurfaceWhite, fontWeight = FontWeight.Black, fontSize = 22.sp)
        Text(label, color = SurfaceWhiteA50, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

/* ========================  GUMBI  ======================== */

@Composable
private fun PrimaryReminderButton(isSet: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = if (isSet) SurfaceBlue01 else SurfaceGold),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Icon(
            imageVector = if (isSet) Icons.Rounded.NotificationsOff else Icons.Rounded.NotificationsActive,
            contentDescription = null,
            tint = if (isSet) SurfaceWhite else SurfaceBlue03,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.size(Gap))
        Text(
            text = if (isSet) stringResource(R.string.home_reminder_cancel) else stringResource(R.string.notif_reminder_button),
            color = if (isSet) SurfaceWhite else SurfaceBlue03,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun SecondaryAction(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(SurfaceBlue02, SurfaceBlue01.copy(alpha = 0.65f))))
            .border(1.dp, SurfaceWhiteA50.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = GapL, horizontal = Gap),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceBlue03.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = SurfaceAccent, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(Gap))
        Text(label, color = SurfaceWhite, fontWeight = FontWeight.Medium, fontSize = 13.sp, textAlign = TextAlign.Center)
    }
}

/* ========================  ZAJEDNIČKE KOMPONENTE  ======================== */

/** Premium kartica: zaobljeni rubovi, tanki rub, suptilni gradijent. */
@Composable
private fun PremiumCard(
    borderColor: androidx.compose.ui.graphics.Color = SurfaceWhiteA50.copy(alpha = 0.16f),
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(listOf(SurfaceBlue02, SurfaceBlue01.copy(alpha = 0.55f), SurfaceBlue03)))
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
    ) { content() }
}

/* ========================  POMOĆNE FUNKCIJE  ======================== */

/** Trenutni vrijeme koje se osvježava u zadanom intervalu (za odbrojavanje/status). */
@Composable
private fun rememberTickingNow(intervalMs: Long): Long {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(intervalMs) {
        while (true) {
            now = System.currentTimeMillis()
            delay(intervalMs)
        }
    }
    return now
}

/* ----- Pozdrav prema statusu sljedeće utakmice ----- */

private enum class GreetingStatus { LIVE, MATCHDAY, TOMORROW, IN_DAYS, FULL_TIME, NONE }

private data class MatchGreeting(val status: GreetingStatus, val days: Long = 0L)

private const val MATCH_DURATION_MINUTES = 120L      // procjena trajanja ako API nema live status
private const val RECENT_FINISH_WINDOW_HOURS = 6L    // koliko dugo nakon kraja vrijedi "Kraj utakmice"

/**
 * Određuje status pozdrava na temelju sljedeće utakmice i trenutnog **lokalnog**
 * vremena (bez hardkodiranih datuma). Čista funkcija – jednostavna za testiranje i
 * proširenje. Koristi kickoff iz API-ja ([Match.kickoffMillis]) pretvoren u lokalnu zonu.
 */
private fun matchGreeting(match: Match?, now: LocalDateTime): MatchGreeting {
    val kickoffMillis = match?.kickoffMillis() ?: return MatchGreeting(GreetingStatus.NONE)
    val kickoff = Instant.ofEpochMilli(kickoffMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    val end = kickoff.plusMinutes(MATCH_DURATION_MINUTES)

    return when {
        now.isBefore(kickoff) -> {
            val days = ChronoUnit.DAYS.between(now.toLocalDate(), kickoff.toLocalDate())
            when {
                days <= 0L -> MatchGreeting(GreetingStatus.MATCHDAY)   // danas, još nije počela
                days == 1L -> MatchGreeting(GreetingStatus.TOMORROW)   // sutra
                else -> MatchGreeting(GreetingStatus.IN_DAYS, days)    // za X dana
            }
        }
        now.isBefore(end) -> MatchGreeting(GreetingStatus.LIVE)        // u tijeku
        ChronoUnit.HOURS.between(end, now) < RECENT_FINISH_WINDOW_HOURS ->
            MatchGreeting(GreetingStatus.FULL_TIME)                    // upravo završila
        else -> MatchGreeting(GreetingStatus.NONE)                    // nema nadolazećih
    }
}

/** Pozdrav (tekst + ikona) prema statusu sljedeće utakmice; osvježava se s vremenom. */
@Composable
private fun greeting(match: Match?): Pair<String, ImageVector> {
    val nowMs = rememberTickingNow(60_000L)
    val now = remember(nowMs) {
        Instant.ofEpochMilli(nowMs).atZone(ZoneId.systemDefault()).toLocalDateTime()
    }
    val greetingInfo = matchGreeting(match, now)
    val icon = when (greetingInfo.status) {
        GreetingStatus.LIVE -> Icons.Rounded.LiveTv
        GreetingStatus.MATCHDAY -> Icons.Rounded.SportsSoccer
        GreetingStatus.TOMORROW -> Icons.Rounded.Event
        GreetingStatus.IN_DAYS -> Icons.Rounded.Schedule
        GreetingStatus.FULL_TIME -> Icons.Rounded.Flag
        GreetingStatus.NONE -> Icons.Rounded.Favorite
    }
    val text = when (greetingInfo.status) {
        GreetingStatus.LIVE -> stringResource(R.string.home_greeting_live)
        GreetingStatus.MATCHDAY -> stringResource(R.string.home_greeting_matchday)
        GreetingStatus.TOMORROW -> stringResource(R.string.home_greeting_tomorrow)
        GreetingStatus.IN_DAYS -> stringResource(R.string.home_greeting_in_days, greetingInfo.days.toInt())
        GreetingStatus.FULL_TIME -> stringResource(R.string.home_greeting_full_time)
        GreetingStatus.NONE -> stringResource(R.string.home_slogan)
    }
    return text to icon
}

private fun statusOf(match: Match, nowMs: Long): MatchStatus {
    if (match.isPlayed) return MatchStatus.FINAL
    val kickoff = match.kickoffMillis()
    if (kickoff != null && nowMs >= kickoff && nowMs < kickoff + 2 * 60 * 60 * 1000L) return MatchStatus.LIVE
    if (match.date == java.time.LocalDate.now().toString()) return MatchStatus.TODAY
    return MatchStatus.UPCOMING
}

/** Stadion: prikazuje se kad Dinamo igra domaću utakmicu (inače null). */
private fun stadiumFor(match: Match, club: ClubInfo?): String? {
    val stadium = club?.stadium?.takeIf { it.isNotBlank() } ?: return null
    val homeIsDinamo = match.homeTeam.name.contains("Dinamo", ignoreCase = true)
    return if (homeIsDinamo) stadium else null
}

/** Prva neodigrana (nadolazeća) utakmica, ili zadnja odigrana ako nema budućih. */
private fun findNextMatch(matches: List<Match>): Match? {
    val upcoming = matches.filter { !it.isPlayed }.minByOrNull { it.date }
    return upcoming ?: matches.firstOrNull()
}
