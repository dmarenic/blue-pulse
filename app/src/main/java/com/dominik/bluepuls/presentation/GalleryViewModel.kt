package com.dominik.bluepuls.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dominik.bluepuls.core.UiState
import com.dominik.bluepuls.data.PhotoRepository
import com.dominik.bluepuls.data.ProfileRepository
import com.dominik.bluepuls.di.ServiceLocator
import com.dominik.bluepuls.domain.Photo
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel za galeriju navijača (upload + prikaz fotografija).
 */
class GalleryViewModel(
    private val auth: FirebaseAuth,
    private val photoRepository: PhotoRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<Photo>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Photo>>> = _state.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    init {
        loadPhotos()
    }

    fun loadPhotos() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            photoRepository.getPhotos(auth.currentUser?.uid)
                .onSuccess { photos ->
                    _state.value = if (photos.isEmpty()) UiState.Empty else UiState.Success(photos)
                }
                .onFailure {
                    _state.value = UiState.Error(it.localizedMessage ?: "Greška pri dohvaćanju fotografija.")
                }
        }
    }

    fun toggleLike(photo: Photo) {
        val uid = auth.currentUser?.uid ?: return
        val like = !photo.isLikedByMe
        // Optimistično ažuriranje (trenutan odziv); revert ako Firestore padne.
        updatePhotoLike(photo.id, like)
        viewModelScope.launch {
            photoRepository.toggleLike(photo.id, uid, like).onFailure {
                updatePhotoLike(photo.id, !like)
                _actionError.value = it.localizedMessage ?: "Lajk nije spremljen."
            }
        }
    }

    private fun updatePhotoLike(photoId: String, liked: Boolean) {
        val current = (_state.value as? UiState.Success)?.data ?: return
        _state.value = UiState.Success(
            current.map { photo ->
                if (photo.id == photoId) {
                    photo.copy(
                        isLikedByMe = liked,
                        likeCount = (photo.likeCount + if (liked) 1 else -1).coerceAtLeast(0)
                    )
                } else {
                    photo
                }
            }
        )
    }

    fun uploadPhoto(uri: Uri, caption: String) {
        val user = auth.currentUser
        if (user == null) {
            _actionError.value = "Niste prijavljeni."
            return
        }
        viewModelScope.launch {
            _isUploading.value = true
            // Ime autora dohvaćamo iz profila (denormaliziramo na fotografiju); ako nije
            // poznato, prikaz će se vratiti na e-mail.
            val displayName = profileRepository.getUserProfile(user.uid).getOrNull()?.displayName.orEmpty()
            photoRepository.uploadPhoto(user.uid, user.email ?: "", displayName, uri, caption)
                .onSuccess { loadPhotos() }
                .onFailure { _actionError.value = it.localizedMessage ?: "Upload nije uspio." }
            _isUploading.value = false
        }
    }

    fun deletePhoto(photo: com.dominik.bluepuls.domain.Photo) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            photoRepository.deletePhoto(photo.id, uid)
                .onSuccess {
                    _actionError.value = "Fotografija je obrisana."
                    loadPhotos()
                }
                .onFailure { _actionError.value = it.localizedMessage ?: "Brisanje nije uspjelo." }
        }
    }

    fun clearActionError() {
        _actionError.value = null
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                GalleryViewModel(
                    ServiceLocator.firebaseAuth,
                    ServiceLocator.photoRepository,
                    ServiceLocator.profileRepository
                )
            }
        }
    }
}
