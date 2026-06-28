package com.dominik.bluepuls.presentation.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.dominik.bluepuls.R
import com.dominik.bluepuls.core.UiState
import com.dominik.bluepuls.domain.Photo
import com.dominik.bluepuls.presentation.GalleryViewModel
import com.dominik.bluepuls.presentation.components.EmptyView
import com.dominik.bluepuls.presentation.components.ErrorView
import com.dominik.bluepuls.presentation.components.LoadingView
import com.dominik.bluepuls.ui.theme.SurfaceAccent
import com.dominik.bluepuls.ui.theme.SurfaceBlue01
import com.dominik.bluepuls.ui.theme.SurfaceBlue02
import com.dominik.bluepuls.ui.theme.SurfaceBlue03
import com.dominik.bluepuls.ui.theme.SurfaceWhite
import com.dominik.bluepuls.ui.theme.SurfaceWhiteA50

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onBack: () -> Unit,
    viewModel: GalleryViewModel = viewModel(factory = GalleryViewModel.Factory)
) {
    val state by viewModel.state.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val actionError by viewModel.actionError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Odabrana slika + komentar preživljavaju rotaciju (Uri je Parcelable, String trivijalan).
    var selectedUri by rememberSaveable { mutableStateOf<android.net.Uri?>(null) }
    var caption by rememberSaveable { mutableStateOf("") }
    // Trenutni pregled / potvrda brisanja su prolazni -> obični remember.
    var selectedPhoto by remember { mutableStateOf<Photo?>(null) }
    var photoToDelete by remember { mutableStateOf<Photo?>(null) }

    val pickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            caption = ""
        }
    }

    LaunchedEffect(actionError) {
        actionError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.gallery_title), color = SurfaceWhite) },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    pickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                containerColor = SurfaceAccent
            ) {
                Icon(Icons.Default.AddAPhoto, contentDescription = stringResource(R.string.gallery_add), tint = SurfaceBlue03)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SurfaceBlue03
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize().background(SurfaceBlue03)) {
            when (val s = state) {
                is UiState.Loading -> LoadingView()
                is UiState.Empty -> EmptyView(message = stringResource(R.string.gallery_empty))
                is UiState.Error -> ErrorView(message = s.message, onRetry = viewModel::loadPhotos)
                is UiState.Success -> PhotoGrid(
                    s.data,
                    onToggleLike = viewModel::toggleLike,
                    onPhotoClick = { selectedPhoto = it }
                )
            }

            // Overlay tijekom uploada - blokira interakciju i jasno pokazuje napredak.
            if (isUploading) {
                UploadingOverlay()
            }
        }
    }

    // Dijalog za opis prije objave.
    selectedUri?.let { uri ->
        CaptionDialog(
            uri = uri,
            caption = caption,
            onCaptionChange = { caption = it },
            onConfirm = {
                viewModel.uploadPhoto(uri, caption)
                selectedUri = null
                caption = ""
            },
            onDismiss = {
                selectedUri = null
                caption = ""
            }
        )
    }

    // Fullscreen pregled fotografije.
    selectedPhoto?.let { photo ->
        PhotoFullscreenDialog(
            photo = photo,
            onDelete = { selectedPhoto = null; photoToDelete = photo },
            onDismiss = { selectedPhoto = null }
        )
    }

    // Potvrda brisanja.
    photoToDelete?.let { photo ->
        DeleteConfirmDialog(
            onConfirm = { viewModel.deletePhoto(photo); photoToDelete = null },
            onDismiss = { photoToDelete = null }
        )
    }
}

@Composable
private fun PhotoFullscreenDialog(photo: Photo, onDelete: () -> Unit, onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(Modifier.fillMaxSize().background(Color(0xF2001123))) {
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = photo.url,
                    contentDescription = stringResource(R.string.cd_photo),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth()
                )
                if (photo.caption.isNotBlank()) {
                    Spacer(Modifier.size(12.dp))
                    Text(photo.caption, color = SurfaceWhite, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.size(4.dp))
                Text(photo.uploaderLabel, color = SurfaceWhiteA50, fontSize = 12.sp)
                if (photo.isMine) {
                    Spacer(Modifier.size(20.dp))
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = SurfaceWhite)
                        Spacer(Modifier.size(6.dp))
                        Text(stringResource(R.string.gallery_delete), color = SurfaceWhite)
                    }
                }
            }
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.gallery_cancel), tint = SurfaceWhite)
            }
        }
    }
}

@Composable
private fun DeleteConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceBlue02,
        title = { Text(stringResource(R.string.gallery_delete_title), color = SurfaceWhite) },
        text = { Text(stringResource(R.string.gallery_delete_confirm), color = SurfaceWhiteA50) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.gallery_delete), color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.gallery_cancel), color = SurfaceWhiteA50) }
        }
    )
}

@Composable
private fun PhotoGrid(photos: List<Photo>, onToggleLike: (Photo) -> Unit, onPhotoClick: (Photo) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().padding(8.dp)
    ) {
        items(photos, key = { it.id }) { photo ->
            PhotoCell(photo, onToggleLike = { onToggleLike(photo) }, onClick = { onPhotoClick(photo) })
        }
    }
}

@Composable
private fun PhotoCell(photo: Photo, onToggleLike: () -> Unit, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(6.dp)
            .background(SurfaceBlue02)
    ) {
        AsyncImage(
            model = photo.url,
            contentDescription = stringResource(R.string.cd_photo),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(SurfaceBlue01).clickable(onClick = onClick)
        )
        if (photo.caption.isNotBlank()) {
            Text(
                text = photo.caption,
                color = SurfaceWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 2.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleLike, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = if (photo.isLikedByMe) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = stringResource(R.string.cd_like),
                    tint = if (photo.isLikedByMe) SurfaceAccent else SurfaceWhiteA50,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text("${photo.likeCount}", color = SurfaceWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            Text(
                text = photo.uploaderLabel,
                color = SurfaceWhiteA50,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun UploadingOverlay() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xCC001123)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = SurfaceAccent)
            Text(
                text = stringResource(R.string.gallery_uploading),
                color = SurfaceWhite,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CaptionDialog(
    uri: android.net.Uri,
    caption: String,
    onCaptionChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceBlue02,
        title = { Text(stringResource(R.string.gallery_add), color = SurfaceWhite) },
        text = {
            Column {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(180.dp).background(SurfaceBlue01)
                )
                OutlinedTextField(
                    value = caption,
                    onValueChange = onCaptionChange,
                    label = { Text(stringResource(R.string.gallery_caption_hint), color = SurfaceWhiteA50) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SurfaceWhite,
                        unfocusedTextColor = SurfaceWhite,
                        focusedBorderColor = SurfaceAccent,
                        unfocusedBorderColor = SurfaceBlue01
                    ),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.gallery_publish), color = SurfaceAccent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.gallery_cancel), color = SurfaceWhiteA50)
            }
        }
    )
}
