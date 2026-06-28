package com.dominik.bluepuls.presentation.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.dominik.bluepuls.R
import com.dominik.bluepuls.core.UiState
import com.dominik.bluepuls.domain.UserProfile
import com.dominik.bluepuls.presentation.ProfileViewModel
import com.dominik.bluepuls.presentation.components.ErrorView
import com.dominik.bluepuls.presentation.components.LoadingView
import com.dominik.bluepuls.ui.theme.SurfaceAccent
import com.dominik.bluepuls.ui.theme.SurfaceBlue01
import com.dominik.bluepuls.ui.theme.SurfaceBlue02
import com.dominik.bluepuls.ui.theme.SurfaceBlue03
import com.dominik.bluepuls.ui.theme.SurfaceGold
import com.dominik.bluepuls.ui.theme.SurfaceWhite
import com.dominik.bluepuls.ui.theme.SurfaceWhiteA50
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
) {
    val state by viewModel.state.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showNameDialog by rememberSaveable { mutableStateOf(false) }

    val avatarPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) viewModel.uploadAvatar(uri) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title), color = SurfaceWhite) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceBlue03)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SurfaceBlue03
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceBlue03)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    when (val s = state) {
                        is UiState.Loading -> LoadingView()
                        is UiState.Error -> ErrorView(message = s.message, onRetry = viewModel::loadProfile)
                        is UiState.Empty -> Unit
                        is UiState.Success -> ProfileContent(
                            profile = s.data,
                            onChangeAvatar = {
                                avatarPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onEditName = { showNameDialog = true }
                        )
                    }
                }

                LogoutButton(onLogout)
                Spacer(Modifier.height(16.dp))
            }

            if (isSaving) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0xCC001123)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SurfaceAccent)
                }
            }
        }
    }

    if (showNameDialog) {
        val current = (state as? UiState.Success)?.data?.displayName.orEmpty()
        EditNameDialog(
            initial = current,
            onConfirm = { viewModel.updateDisplayName(it); showNameDialog = false },
            onDismiss = { showNameDialog = false }
        )
    }
}

@Composable
private fun ProfileContent(
    profile: UserProfile,
    onChangeAvatar: () -> Unit,
    onEditName: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))
        ProfileAvatar(avatarUrl = profile.avatarUrl, onChangeAvatar = onChangeAvatar)

        Spacer(Modifier.height(16.dp))

        // Ime (ili email ako ime nije postavljeno) + uredi
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = profile.displayName.ifBlank { profile.email },
                color = SurfaceWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onEditName) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.profile_edit_name), tint = SurfaceAccent, modifier = Modifier.size(18.dp))
            }
        }
        if (profile.displayName.isNotBlank()) {
            Text(text = profile.email, color = SurfaceWhiteA50, fontSize = 13.sp)
        }
        Text(
            text = stringResource(R.string.profile_favorite_team, "Dinamo Zagreb"),
            color = SurfaceWhiteA50,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(8.dp))
        profile.createdAtMillis?.let {
            Text(stringResource(R.string.profile_member_since, formatDate(it)), color = SurfaceWhiteA50, fontSize = 12.sp)
        }

        Spacer(Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceBlue02)
        ) {
            Column(Modifier.padding(20.dp)) {
                StatRow(stringResource(R.string.profile_votes_cast), profile.votesCast.toString(), highlight = true)
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = SurfaceWhiteA50.copy(alpha = 0.1f))
                Spacer(Modifier.height(12.dp))
                StatRow(stringResource(R.string.profile_photos_uploaded), profile.photosUploaded.toString())
                Spacer(Modifier.height(12.dp))
                StatRow(stringResource(R.string.profile_favorite_player), profile.favoritePlayer.ifBlank { "—" })
            }
        }
    }
}

@Composable
private fun ProfileAvatar(avatarUrl: String?, onChangeAvatar: () -> Unit) {
    Box(modifier = Modifier.size(110.dp), contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(SurfaceBlue02)
                .clickable(onClick = onChangeAvatar),
            contentAlignment = Alignment.Center
        ) {
            if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = stringResource(R.string.cd_profile_avatar),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Icon(Icons.Default.Person, contentDescription = stringResource(R.string.cd_profile_avatar), tint = SurfaceAccent, modifier = Modifier.size(60.dp))
            }
        }
        // Mali badge s kamerom (affordance za promjenu)
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(SurfaceAccent)
                .clickable(onClick = onChangeAvatar),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = stringResource(R.string.cd_change_avatar), tint = SurfaceBlue03, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun EditNameDialog(initial: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceBlue02,
        title = { Text(stringResource(R.string.profile_edit_name), color = SurfaceWhite) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                placeholder = { Text(stringResource(R.string.profile_name_hint), color = SurfaceWhiteA50) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SurfaceWhite,
                    unfocusedTextColor = SurfaceWhite,
                    focusedBorderColor = SurfaceAccent,
                    unfocusedBorderColor = SurfaceBlue01
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }) {
                Text(stringResource(R.string.profile_save), color = SurfaceAccent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.gallery_cancel), color = SurfaceWhiteA50) }
        }
    )
}

@Composable
private fun StatRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = SurfaceWhite, fontWeight = if (highlight) FontWeight.Medium else FontWeight.Normal)
        Text(
            text = value,
            color = if (highlight) SurfaceAccent else SurfaceWhite,
            fontWeight = FontWeight.Bold,
            fontSize = if (highlight) 22.sp else 16.sp
        )
    }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    Button(
        onClick = onLogout,
        colors = ButtonDefaults.buttonColors(containerColor = SurfaceGold),
        modifier = Modifier.fillMaxWidth().height(50.dp)
    ) {
        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = SurfaceBlue03)
        Spacer(Modifier.size(8.dp))
        Text(stringResource(R.string.auth_logout), color = SurfaceBlue03, fontWeight = FontWeight.Bold)
    }
}

private fun formatDate(millis: Long): String {
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy."))
}
