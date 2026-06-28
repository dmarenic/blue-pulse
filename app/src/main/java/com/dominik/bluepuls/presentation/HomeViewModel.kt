package com.dominik.bluepuls.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dominik.bluepuls.data.ClubRepository
import com.dominik.bluepuls.di.ServiceLocator
import com.dominik.bluepuls.domain.ClubInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel za početni ekran - dohvaća podatke o klubu (grb, stadion).
 */
class HomeViewModel(
    private val clubRepository: ClubRepository
) : ViewModel() {

    private val _clubInfo = MutableStateFlow<ClubInfo?>(null)
    val clubInfo: StateFlow<ClubInfo?> = _clubInfo.asStateFlow()

    init {
        loadClubInfo()
    }

    fun loadClubInfo() {
        viewModelScope.launch {
            clubRepository.getClubInfo().onSuccess { _clubInfo.value = it }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { HomeViewModel(ServiceLocator.clubRepository) }
        }
    }
}
