package com.dominik.bluepuls.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dominik.bluepuls.core.UiState
import com.dominik.bluepuls.data.StandingsRepository
import com.dominik.bluepuls.di.ServiceLocator
import com.dominik.bluepuls.domain.Standing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel za ljestvicu HNL-a.
 */
class StandingsViewModel(
    private val repository: StandingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<Standing>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Standing>>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            repository.getStandings()
                .onSuccess { standings ->
                    _state.value = if (standings.isEmpty()) UiState.Empty else UiState.Success(standings)
                }
                .onFailure {
                    _state.value = UiState.Error(it.localizedMessage ?: "Greška pri dohvaćanju ljestvice.")
                }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { StandingsViewModel(ServiceLocator.standingsRepository) }
        }
    }
}
