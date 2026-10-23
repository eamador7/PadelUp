package com.padelscoring.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val selectedCourts: Int = 2,
    val isGroupMode: Boolean = false,
    val playerCountOptions: List<Int> = listOf(4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 16, 20, 24, 28, 32),
    val targetPointOptions: List<Int> = listOf(21, 24, 32),
    val courtOptions: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7, 8),
    val playerRoster: List<String> = listOf(
        "Player 1", "Player 2", "Player 3", "Player 4",
        "Player 5", "Player 6", "Player 7", "Player 8"
    ),
    val tournamentName: String = "Offline Americano",
    val isLaunching: Boolean = false,
    val errorMessage: String? = null
) {
    val restingPlayersCount: Int
        get() = maxOf(0, selectedPlayerCount - (selectedCourts * 4))
}

/**
 * MVI / StateFlow ViewModel powering the Offline Quick Launcher screen.
 */
class OfflineLauncherViewModel(
    private val tournamentRepository: TournamentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OfflineLauncherUiState())
    val uiState: StateFlow<OfflineLauncherUiState> = _uiState.asStateFlow()

    fun setPlayerCount(count: Int) {
        val clamped = count.coerceIn(4, 32)
        val currentRoster = _uiState.value.playerRoster.toMutableList()
        while (currentRoster.size < clamped) {
            currentRoster.add("Player ${currentRoster.size + 1}")
        }
        val defaultCourts = maxOf(1, minOf(8, clamped / 4))
        _uiState.value = _uiState.value.copy(
            selectedPlayerCount = clamped,
            selectedCourts = defaultCourts,
            playerRoster = currentRoster
        )
    }

    fun incrementPlayerCount() {
        if (_uiState.value.selectedPlayerCount < 32) {
            setPlayerCount(_uiState.value.selectedPlayerCount + 1)
        }
    }

    fun decrementPlayerCount() {
        if (_uiState.value.selectedPlayerCount > 4) {
            setPlayerCount(_uiState.value.selectedPlayerCount - 1)
        }
    }

    fun setCourtCount(courts: Int) {
        val maxPossible = maxOf(1, minOf(8, _uiState.value.selectedPlayerCount / 4))
        val clamped = courts.coerceIn(1, maxPossible)
        _uiState.value = _uiState.value.copy(selectedCourts = clamped)
    }

    fun setGroupMode(isGroup: Boolean) {
        _uiState.value = _uiState.value.copy(
            isGroupMode = isGroup,
            tournamentName = if (isGroup) "Club Americano" else "Offline Americano"
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
     * Creates the tournament and invokes [onLaunched] with the new tournamentId.
     */
    fun kickoffTournament(onLaunched: (tournamentId: String) -> Unit) {
        val state = _uiState.value
        val players = state.playerRoster.take(state.selectedPlayerCount)
        if (players.size < 4) {
            _uiState.value = _uiState.value.copy(errorMessage = "At least 4 players required")
            return
        }

        val effectiveCourts = minOf(state.selectedCourts, maxOf(1, players.size / 4))

        _uiState.value = _uiState.value.copy(isLaunching = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val tournamentId = tournamentRepository.createAmericanoTournament(
                    name = state.tournamentName,
                    playerNames = players,
                    targetPoints = state.selectedTargetPoints,
                    totalCourts = effectiveCourts,
                    isOffline = !state.isGroupMode
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
}
