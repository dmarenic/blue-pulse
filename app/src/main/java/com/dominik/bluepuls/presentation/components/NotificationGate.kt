package com.dominik.bluepuls.presentation.components

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/**
 * Vraća akciju koja prvo osigura POST_NOTIFICATIONS dozvolu (Android 13+),
 * pa tek onda pokrene [onGranted]. Na starijim verzijama pokreće odmah.
 *
 * Koristi se na svakom mjestu gdje korisnik traži notifikaciju (Home, Detalji),
 * bez dupliciranja permission logike.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberNotificationGate(onGranted: () -> Unit): () -> Unit {
    val permission = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    var pending by remember { mutableStateOf(false) }
    val latestOnGranted by rememberUpdatedState(onGranted)

    LaunchedEffect(permission.status) {
        if (pending && permission.status.isGranted) {
            pending = false
            latestOnGranted()
        }
    }

    return {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permission.status.isGranted) {
            pending = true
            permission.launchPermissionRequest()
        } else {
            latestOnGranted()
        }
    }
}
