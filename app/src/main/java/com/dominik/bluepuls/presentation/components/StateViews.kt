package com.dominik.bluepuls.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dominik.bluepuls.R
import com.dominik.bluepuls.ui.theme.SurfaceAccent
import com.dominik.bluepuls.ui.theme.SurfaceBlue01
import com.dominik.bluepuls.ui.theme.SurfaceWhite
import com.dominik.bluepuls.ui.theme.SurfaceWhiteA50

/** Centriran spinner za Loading stanje. */
@Composable
fun LoadingView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = SurfaceAccent)
    }
}

/** Poruka + gumb za ponovni pokušaj za Error stanje. */
@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = SurfaceWhite,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceBlue01),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(stringResource(R.string.action_retry), color = SurfaceWhite)
        }
    }
}

/** Poruka za Empty stanje (dohvat uspio, ali nema podataka). */
@Composable
fun EmptyView(
    message: String = stringResource(R.string.state_empty),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, color = SurfaceWhiteA50, textAlign = TextAlign.Center)
    }
}
