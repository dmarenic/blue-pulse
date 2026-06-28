package com.dominik.bluepuls.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dominik.bluepuls.core.DataOrigin
import com.dominik.bluepuls.core.UiState
import com.dominik.bluepuls.data.MatchRepository
import com.dominik.bluepuls.data.ReminderRepository
import com.dominik.bluepuls.di.ServiceLocator
import com.dominik.bluepuls.domain.Match
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Filter prikaza utakmica na ekranu (toggle). */
enum class MatchFilter { UPCOMING, PLAYED }

/**
 * ViewModel za utakmice (nadolazeće + odigrane s TheSportsDB-a).
 * Stanje preko [UiState] - greška API-ja postaje Error stanje, ne rušenje.
 */
class MatchesViewModel(
    private val repository: MatchRepository,
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<Match>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Match>>> = _state.asStateFlow()

    private val _origin = MutableStateFlow<DataOrigin?>(null)
    val origin: StateFlow<DataOrigin?> = _origin.asStateFlow()

    /** ID-evi utakmica za koje je postavljen podsjetnik. */
    val remindedMatchIds: StateFlow<Set<String>> = reminderRepository.remindedMatchIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    // Toggle: koja kategorija utakmica se prikazuje (početno nadolazeće).
    private val _filter = MutableStateFlow(MatchFilter.UPCOMING)
    val filter: StateFlow<MatchFilter> = _filter.asStateFlow()

    fun setFilter(value: MatchFilter) {
        _filter.value = value
    }

    init {
        loadMatches()
    }

    /** Uključi/isključi podsjetnik za utakmicu. */
    fun toggleReminder(match: Match) {
        viewModelScope.launch {
            if (remindedMatchIds.value.contains(match.id)) {
                reminderRepository.cancelReminder(match.id)
                _message.value = "Podsjetnik otkazan."
            } else {
                val scheduled = reminderRepository.setReminder(match)
                _message.value = if (scheduled) {
                    "Podsjetnik postavljen — javit ćemo se prije utakmice! 🔔"
                } else {
                    "Utakmica je već odigrana."
                }
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun loadMatches() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val sourced = repository.getDinamoMatches()
                _origin.value = sourced.origin
                _state.value = if (sourced.data.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(sourced.data)
                }
            } catch (e: Exception) {
                _state.value = UiState.Error(e.localizedMessage ?: "Neočekivana greška.")
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                MatchesViewModel(
                    ServiceLocator.matchRepository,
                    ServiceLocator.reminderRepository
                )
            }
        }
    }
}
