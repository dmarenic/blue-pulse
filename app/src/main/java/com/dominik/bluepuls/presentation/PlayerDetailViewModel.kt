package com.dominik.bluepuls.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dominik.bluepuls.core.UiState
import com.dominik.bluepuls.data.PlayerRepository
import com.dominik.bluepuls.data.VoteRepository
import com.dominik.bluepuls.di.ServiceLocator
import com.dominik.bluepuls.domain.PlayerDetails
import com.dominik.bluepuls.domain.PlayerOfMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel za detalje igrača. Učitava podatke s TheSportsDB-a + osvojene
 * titule igrača mjeseca (iz Firestore povijesti).
 */
class PlayerDetailViewModel(
    private val playerId: String,
    private val playerRepository: PlayerRepository,
    private val voteRepository: VoteRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<PlayerDetails>>(UiState.Loading)
    val state: StateFlow<UiState<PlayerDetails>> = _state.asStateFlow()

    private val _titles = MutableStateFlow<List<PlayerOfMonth>>(emptyList())
    val titles: StateFlow<List<PlayerOfMonth>> = _titles.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            playerRepository.getPlayerDetails(playerId)
                .onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Error(it.localizedMessage ?: "Detalji nisu dostupni.") }
        }
        viewModelScope.launch {
            voteRepository.titlesForPlayer(playerId).onSuccess { _titles.value = it }
        }
    }

    companion object {
        fun factory(playerId: String) = viewModelFactory {
            initializer {
                PlayerDetailViewModel(
                    playerId,
                    ServiceLocator.playerRepository,
                    ServiceLocator.voteRepository
                )
            }
        }
    }
}
