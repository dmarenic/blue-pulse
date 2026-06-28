package com.dominik.bluepuls.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dominik.bluepuls.core.VotingPeriod
import com.dominik.bluepuls.data.VoteRepository
import com.dominik.bluepuls.di.ServiceLocator
import com.dominik.bluepuls.domain.PlayerOfMonth
import com.dominik.bluepuls.domain.VoteCount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Trenutni rezultati glasanja za period. */
data class CurrentResults(
    val periodLabel: String,
    val entries: List<VoteCount>,
    val totalVotes: Int
)

/**
 * ViewModel za ekran "Igrač mjeseca": trenutni rezultati + povijest pobjednika.
 * Pri svakom otvaranju idempotentno zaključi prethodni mjesec (spremi pobjednika).
 */
class PlayerOfMonthViewModel(
    private val voteRepository: VoteRepository
) : ViewModel() {

    private val period = VotingPeriod.current()

    private val _current = MutableStateFlow<CurrentResults?>(null)
    val current: StateFlow<CurrentResults?> = _current.asStateFlow()

    private val _history = MutableStateFlow<List<PlayerOfMonth>>(emptyList())
    val history: StateFlow<List<PlayerOfMonth>> = _history.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            // Zaključi prethodni mjesec (idempotentno - ne prepisuje postojećeg pobjednika).
            voteRepository.closePreviousMonth()

            val board = voteRepository.leaderboard(period).getOrDefault(emptyList())
            _current.value = CurrentResults(
                periodLabel = VotingPeriod.label(period),
                entries = board,
                totalVotes = board.sumOf { it.count }
            )

            voteRepository.getHistory().onSuccess { _history.value = it }
            _isLoading.value = false
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { PlayerOfMonthViewModel(ServiceLocator.voteRepository) }
        }
    }
}
