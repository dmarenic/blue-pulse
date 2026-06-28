package com.dominik.bluepuls.presentation.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dominik.bluepuls.R
import com.dominik.bluepuls.core.Constants
import com.dominik.bluepuls.presentation.MapViewModel
import com.dominik.bluepuls.ui.theme.SurfaceAccent
import com.dominik.bluepuls.ui.theme.SurfaceBlue01
import com.dominik.bluepuls.ui.theme.SurfaceBlue02
import com.dominik.bluepuls.ui.theme.SurfaceBlue03
import com.dominik.bluepuls.ui.theme.SurfaceGold
import com.dominik.bluepuls.ui.theme.SurfaceWhite
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    onBack: () -> Unit,
    viewModel: MapViewModel = viewModel(factory = MapViewModel.Factory)
) {
    val userLocation by viewModel.userLocation.collectAsState()
    val distanceKm by viewModel.distanceKm.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val directionsErrorText = stringResource(R.string.map_directions_error)

    // Otvara Google Maps navigaciju (turn-by-turn) od trenutne lokacije do Maksimira.
    // Univerzalni maps URL -> Google Maps app ako je instaliran, inače preglednik.
    val openDirections: () -> Unit = {
        try {
            val uri = Uri.parse(
                "https://www.google.com/maps/dir/?api=1" +
                    "&destination=${Constants.MAKSIMIR_LAT},${Constants.MAKSIMIR_LNG}" +
                    "&travelmode=driving"
            )
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: Exception) {
            scope.launch { snackbarHostState.showSnackbar(directionsErrorText) }
        }
    }

    val locationPermissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    val granted = locationPermissions.allPermissionsGranted

    val maksimir = LatLng(Constants.MAKSIMIR_LAT, Constants.MAKSIMIR_LNG)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(maksimir, 15f)
    }

    LaunchedEffect(granted) {
        if (granted) viewModel.fetchUserLocation()
    }
    LaunchedEffect(userLocation) {
        userLocation?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 14f)
            )
        }
    }
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.map_title), color = SurfaceWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = SurfaceWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceBlue03)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SurfaceBlue03
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = granted),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = granted,
                    zoomControlsEnabled = true
                )
            ) {
                Marker(
                    state = rememberMarkerState(position = maksimir),
                    title = stringResource(R.string.map_title),
                    snippet = stringResource(R.string.maksimir_snippet)
                )
            }

            distanceKm?.let { km ->
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceBlue02)
                ) {
                    Text(
                        text = stringResource(
                            R.string.map_distance,
                            String.format(Locale.getDefault(), "%.1f", km)
                        ),
                        color = SurfaceWhite,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (!granted) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceBlue02),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                stringResource(R.string.map_permission_rationale),
                                color = SurfaceWhite
                            )
                            Spacer(Modifier.padding(4.dp))
                            Button(
                                onClick = { locationPermissions.launchMultiplePermissionRequest() },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceBlue01)
                            ) {
                                Text(stringResource(R.string.map_grant_permission), color = SurfaceWhite)
                            }
                        }
                    }
                    Spacer(Modifier.padding(6.dp))
                }

                // Navigacija do Maksimira (otvara Google Maps upute).
                Button(
                    onClick = openDirections,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        stringResource(R.string.map_directions),
                        color = SurfaceBlue03,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.padding(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(maksimir, 15f)
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceAccent),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.map_center_stadium), color = SurfaceBlue03, fontWeight = FontWeight.Bold)
                    }
                    if (granted) {
                        Button(
                            onClick = {
                                // Direktno animiraj na poznatu lokaciju (radi i kad se vrijednost
                                // nije promijenila) + osvježi je u pozadini.
                                userLocation?.let { loc ->
                                    scope.launch {
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(loc.latitude, loc.longitude), 15f
                                            )
                                        )
                                    }
                                }
                                viewModel.fetchUserLocation()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceBlue01),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.map_center_me), color = SurfaceWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
