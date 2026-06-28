package com.dominik.bluepuls.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dominik.bluepuls.data.AuthRepository
import com.dominik.bluepuls.data.ProfileRepository
import com.dominik.bluepuls.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel za autentifikaciju (login / register / logout).
 * AuthRepository se injektira preko [Factory] (DI).
 */
class AuthViewModel(
    private val repository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _isUserLoggedIn = MutableStateFlow(repository.currentUser != null)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun login(email: String, password: String) {
        val validationError = validateLogin(email, password)
        if (validationError != null) {
            _errorMessage.value = validationError
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.login(email, password)
                .onSuccess { user ->
                    // Osiguraj da postoji Firestore profil (npr. za starije račune).
                    profileRepository.ensureUserDocument(user.uid, user.email ?: email.trim())
                    _isUserLoggedIn.value = true
                }
                .onFailure { _errorMessage.value = it.localizedMessage ?: "Greška pri prijavi." }
            _isLoading.value = false
        }
    }

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        // Validacija prije svega: ako ne prođe, ostaje na formi - bez loadinga i bez poziva Firebasea.
        val validationError = validateRegister(name, email, password, confirmPassword)
        if (validationError != null) {
            _errorMessage.value = validationError
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.register(email, password)
                .onSuccess { user ->
                    // Kreiraj Firestore profil za novog korisnika (s upisanim imenom).
                    profileRepository.ensureUserDocument(user.uid, user.email ?: email.trim(), name.trim())
                    _isUserLoggedIn.value = true
                }
                .onFailure { _errorMessage.value = it.localizedMessage ?: "Greška pri registraciji." }
            _isLoading.value = false
        }
    }

    fun logout() {
        repository.logout()
        _isUserLoggedIn.value = false
    }

    /** Čisti poruku o grešci (npr. pri prelasku Login <-> Register). */
    fun clearError() {
        _errorMessage.value = null
    }

    private fun validateLogin(email: String, password: String): String? = when {
        email.isBlank() || password.isBlank() -> "Upišite email i lozinku."
        else -> emailFormatError(email) ?: passwordLengthError(password)
    }

    private fun validateRegister(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): String? = when {
        name.isBlank() -> "Upišite svoje ime."
        email.isBlank() -> "Upišite email adresu."
        password.isBlank() || confirmPassword.isBlank() -> "Upišite i potvrdite lozinku."
        else -> emailFormatError(email)
            ?: passwordLengthError(password)
            ?: if (password != confirmPassword) "Lozinke se ne podudaraju." else null
    }

    // Zajedničke provjere (bez dupliciranja logike između prijave i registracije).
    private fun emailFormatError(email: String): String? =
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches())
            "Email nije u ispravnom formatu." else null

    private fun passwordLengthError(password: String): String? =
        if (password.length < 6) "Lozinka mora imati barem 6 znakova." else null

    companion object {
        val Factory = viewModelFactory {
            initializer {
                AuthViewModel(
                    ServiceLocator.authRepository,
                    ServiceLocator.profileRepository
                )
            }
        }
    }
}
