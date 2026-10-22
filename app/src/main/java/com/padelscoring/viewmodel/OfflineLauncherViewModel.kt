package com.padelscoring.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.padelscoring.data.repository.TournamentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for the Offline Quick Launcher screen.
 */
data class OfflineLauncherUiState(
    val selectedPlayerCount: Int = 8,
    val selectedTargetPoints: Int = 32,
    val playerCountOptions: List<Int> = listOf(4, 8, 12, 16),
    val targetPointOptions: List<Int> = listOf(21, 24, 32),
    val playerRoster: List<String> = listOf(
        "Player 1", "Player 2", "Player 3", "Player 4",
        "Player 5", "Player 6", "Player 7", "Player 8"
    ),
    val tournamentName: String = "Offline Americano",
    val isLaunching: Boolean = false,
    val errorMessage: String? = null
)

/**
 * MVI / StateFlow ViewModel powering the Offline Quick Launcher screen.
 */
class OfflineLauncherViewModel(
    private val tournamentRepository: TournamentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OfflineLauncherUiState())
    val uiState: StateFlow<OfflineLauncherUiState> = _uiState.asStateFlow()

    fun setPlayerCount(count: Int) {
        val currentRoster = _uiState.value.playerRoster.toMutableList()
        while (currentRoster.size < count) {
            currentRoster.add("Player ${currentRoster.size + 1}")
        }
        _uiState.value = _uiState.value.copy(
            selectedPlayerCount = count,
            playerRoster = currentRoster
        )
    }

    fun setTargetPoints(points: Int) {
        _uiState.value = _uiState.value.copy(selectedTargetPoints = points)
    }

    fun setTournamentName(name: String) {
        _uiState.value = _uiState.value.copy(tournamentName = name)
    }

    fun addGuestPlayer(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val updated = _uiState.value.playerRoster + trimmed
        _uiState.value = _uiState.value.copy(playerRoster = updated)
    }

    fun removeGuestPlayer(index: Int) {
        if (index in _uiState.value.playerRoster.indices) {
            val updated = _uiState.value.playerRoster.toMutableList()
            updated.removeAt(index)
            _uiState.value = _uiState.value.copy(playerRoster = updated)
        }
    }

    fun updateGuestName(index: Int, name: String) {
        if (index in _uiState.value.playerRoster.indices) {
            val updated = _uiState.value.playerRoster.toMutableList()
            updated[index] = name
            _uiState.value = _uiState.value.copy(playerRoster = updated)
        }
    }

    /**
     * Creates the local offline tournament and invokes [onLaunched] with the new tournamentId.
     */
    fun kickoffTournament(onLaunched: (tournamentId: String) -> Unit) {
        val state = _uiState.value
        val players = state.playerRoster.take(state.selectedPlayerCount)
        if (players.size < 4) {
            _uiState.value = _uiState.value.copy(errorMessage = "At least 4 players required")
            return
        }

        _uiState.value = _uiState.value.copy(isLaunching = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val tournamentId = tournamentRepository.createAmericanoTournament(
                    name = state.tournamentName,
                    playerNames = players,
                    targetPoints = state.selectedTargetPoints,
                    totalCourts = state.selectedPlayerCount / 4,
                    isOffline = true
                )
                _uiState.value = _uiState.value.copy(isLaunching = false)
                onLaunched(tournamentId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLaunching = false,
                    errorMessage = e.message
                )
            }
        }
    }

    /**
     * NavController convenience overload: creates tournament and navigates to Americano Command Center.
     */
    fun kickoffTournament(navController: NavController) {
        kickoffTournament { tournamentId ->
            navController.navigate("americano?id=$tournamentId")
        }
    }
}
