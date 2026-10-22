package com.padelscoring.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.padelscoring.data.repository.CourtMatchUiState
import com.padelscoring.data.repository.TournamentRepository
import com.padelscoring.ui.screens.AmericanoPlayerStanding
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for the Americano Command Center screen.
 */
data class AmericanoUiState(
    val tournamentId: String = "",
    val tournamentName: String = "Americano Tournament",
    val currentRoundNumber: Int = 1,
    val totalRounds: Int = 1,
    val targetPoints: Int = 32,
    val courtMatches: List<CourtMatchUiState> = emptyList(),
    val standings: List<AmericanoPlayerStanding> = emptyList(),
    val canGenerateNextRound: Boolean = false,
    val isFinished: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * MVI / StateFlow ViewModel powering the Americano Command Center screen.
 */
class AmericanoTournamentViewModel(
    private val tournamentRepository: TournamentRepository,
    initialTournamentId: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AmericanoUiState(
            tournamentId = initialTournamentId ?: "",
            isLoading = initialTournamentId != null
        )
    )
    val uiState: StateFlow<AmericanoUiState> = _uiState.asStateFlow()

    private var observationJob: Job? = null

    init {
        if (initialTournamentId != null && initialTournamentId.isNotEmpty()) {
            loadTournament(initialTournamentId)
        }
    }

    /**
     * Connects this ViewModel to observe a specific tournament from repository.
     */
    fun loadTournament(tournamentId: String) {
        observationJob?.cancel()
        _uiState.value = _uiState.value.copy(
            tournamentId = tournamentId,
            isLoading = true,
            errorMessage = null
        )

        observationJob = viewModelScope.launch {
            try {
                tournamentRepository.observeTournament(tournamentId).collect { repoState ->
                    _uiState.value = _uiState.value.copy(
                        tournamentId = tournamentId,
                        tournamentName = repoState.tournament?.name ?: _uiState.value.tournamentName,
                        currentRoundNumber = repoState.currentRoundNumber,
                        totalRounds = repoState.totalRounds,
                        targetPoints = repoState.targetPoints,
                        courtMatches = repoState.courtMatches,
                        standings = repoState.standings,
                        canGenerateNextRound = repoState.canGenerateNextRound,
                        isFinished = repoState.isFinished,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    /**
     * Updates scores for a specific match, enforcing point conservation ($t1 + t2 == targetPoints$).
     */
    fun onScoreChanged(matchId: String, t1: Int, t2: Int) {
        viewModelScope.launch {
            try {
                tournamentRepository.recordMatchScore(matchId, t1, t2)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    /**
     * Generates and activates the next tournament round.
     */
    fun onGenerateNextRound() {
        val currentTournamentId = _uiState.value.tournamentId
        if (currentTournamentId.isEmpty()) return

        viewModelScope.launch {
            try {
                tournamentRepository.advanceToNextRound(currentTournamentId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }
}
