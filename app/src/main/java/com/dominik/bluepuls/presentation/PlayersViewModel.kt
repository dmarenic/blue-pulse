package com.dominik.bluepuls.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dominik.bluepuls.core.DataOrigin
import com.dominik.bluepuls.core.UiState
import com.dominik.bluepuls.core.VotingPeriod
import com.dominik.bluepuls.data.PlayerRepository
import com.dominik.bluepuls.data.ProfileRepository
import com.dominik.bluepuls.data.VoteRepository
import com.dominik.bluepuls.di.ServiceLocator
import com.dominik.bluepuls.domain.Player
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Kriteriji sortiranja igrača. */
enum class PlayerSort { SURNAME, NUMBER, VOTES }

/**
 * ViewModel za ekran "Igrači" (uključuje glasanje za igrača mjeseca).
 *
 * Glasanje je MJESEČNO i promjenjivo: izvor istine je Firestore
 * (jedan glas po korisniku po periodu), pa glas radi i nakon reinstalacije.
 */
class PlayersViewModel(
    private val playerRepository: PlayerRepository,
    private val voteRepository: VoteRepository,
    private val profileRepository: ProfileRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val period = VotingPeriod.current()

    private val _rawPlayers = MutableStateFlow<UiState<List<Player>>>(UiState.Loading)

    private val _origin = MutableStateFlow<DataOrigin?>(null)
    val origin: StateFlow<DataOrigin?> = _origin.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sort = MutableStateFlow(PlayerSort.NUMBER)
    val sort: StateFlow<PlayerSort> = _sort.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    /** Igrač za kojeg je korisnik glasao ovaj mjesec (Firestore + optimistično). */
    private val _votedPlayerId = MutableStateFlow<String?>(null)
    val votedPlayerId: StateFlow<String?> = _votedPlayerId.asStateFlow()

    /** Broj glasova po igraču (za sortiranje po glasovima). */
    private val _voteCounts = MutableStateFlow<Map<String, Int>>(emptyMap())

    /** Konačna lista za prikaz: filtrirana (pretraga) + sortirana. */
    val players: StateFlow<UiState<List<Player>>> =
        combine(_rawPlayers, _searchQuery, _sort, _voteCounts) { raw, query, sort, counts ->
            if (raw !is UiState.Success) return@combine raw
            val filtered = if (query.isBlank()) {
                raw.data
            } else {
                raw.data.filter { it.name.contains(query.trim(), ignoreCase = true) }
            }
            val sorted = when (sort) {
                PlayerSort.SURNAME -> filtered.sortedBy { it.lastName.lowercase() }
                PlayerSort.NUMBER -> filtered.sortedBy { it.number ?: Int.MAX_VALUE }
                PlayerSort.VOTES -> filtered.sortedByDescending { counts[it.id] ?: 0 }
            }
            if (sorted.isEmpty()) UiState.Empty else UiState.Success(sorted)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    init {
        loadPlayers()
        loadVoteData()
    }

    fun loadPlayers() {
        viewModelScope.launch {
            _rawPlayers.value = UiState.Loading
            try {
                val sourced = playerRepository.getDinamoPlayers()
                _origin.value = sourced.origin
                _rawPlayers.value = if (sourced.data.isEmpty()) UiState.Empty else UiState.Success(sourced.data)
            } catch (e: Exception) {
                _rawPlayers.value = UiState.Error(e.localizedMessage ?: "Greška pri dohvaćanju igrača.")
            }
        }
    }

    private fun loadVoteData() {
        viewModelScope.launch {
            _votedPlayerId.value = voteRepository.currentVote(period).getOrNull()
            voteRepository.leaderboard(period).onSuccess { board ->
                _voteCounts.value = board.associate { it.playerId to it.count }
            }
        }
    }

    fun setQuery(query: String) { _searchQuery.value = query }

    fun setSort(option: PlayerSort) { _sort.value = option }

    fun vote(player: Player) {
        val previous = _votedPlayerId.value
        _votedPlayerId.value = player.id // optimistično
        viewModelScope.launch {
            voteRepository.castVote(player.id, player.name, period)
                .onSuccess {
                    auth.currentUser?.uid?.let { profileRepository.setFavoritePlayer(it, player.name) }
                    _message.value = "Glasao si za ${player.name}! 💙"
                    loadVoteData()
                }
                .onFailure {
                    _votedPlayerId.value = previous // revert
                    _message.value = it.localizedMessage ?: "Glasanje nije uspjelo."
                }
        }
    }

    fun removeVote() {
        _votedPlayerId.value = null
        viewModelScope.launch {
            voteRepository.removeVote(period)
            auth.currentUser?.uid?.let { profileRepository.setFavoritePlayer(it, null) }
            _message.value = "Glas je uklonjen."
            loadVoteData()
        }
    }

    fun clearMessage() { _message.value = null }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                PlayersViewModel(
                    ServiceLocator.playerRepository,
                    ServiceLocator.voteRepository,
                    ServiceLocator.profileRepository,
                    ServiceLocator.firebaseAuth
                )
            }
        }
    }
}
