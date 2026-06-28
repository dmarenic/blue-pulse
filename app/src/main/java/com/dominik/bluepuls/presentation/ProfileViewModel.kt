package com.dominik.bluepuls.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dominik.bluepuls.core.UiState
import com.dominik.bluepuls.data.ProfileRepository
import com.dominik.bluepuls.di.ServiceLocator
import com.dominik.bluepuls.domain.UserProfile
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel za profil. Dohvaća [UserProfile] iz Firestorea preko repozitorija.
 */
class ProfileViewModel(
    private val auth: FirebaseAuth,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<UserProfile>>(UiState.Loading)
    val state: StateFlow<UiState<UserProfile>> = _state.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        loadProfile()
    }

    fun updateDisplayName(name: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isSaving.value = true
            profileRepository.updateDisplayName(uid, name)
                .onSuccess { _message.value = "Ime je spremljeno."; loadProfile() }
                .onFailure { _message.value = it.localizedMessage ?: "Spremanje nije uspjelo." }
            _isSaving.value = false
        }
    }

    fun uploadAvatar(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isSaving.value = true
            profileRepository.uploadAvatar(uid, uri)
                .onSuccess { _message.value = "Profilna slika ažurirana."; loadProfile() }
                .onFailure { _message.value = it.localizedMessage ?: "Upload nije uspio." }
            _isSaving.value = false
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun loadProfile() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _state.value = UiState.Error("Korisnik nije prijavljen.")
            return
        }
        viewModelScope.launch {
            _state.value = UiState.Loading
            profileRepository.getUserProfile(uid)
                .onSuccess { _state.value = UiState.Success(it) }
                .onFailure {
                    _state.value = UiState.Error(it.localizedMessage ?: "Greška pri dohvaćanju profila.")
                }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                ProfileViewModel(
                    ServiceLocator.firebaseAuth,
                    ServiceLocator.profileRepository
                )
            }
        }
    }
}
