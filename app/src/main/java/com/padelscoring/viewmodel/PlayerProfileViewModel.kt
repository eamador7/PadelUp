package com.padelscoring.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.padelscoring.data.repository.UserProfileUiState
import com.padelscoring.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for the Player Profile and Stats screen.
 */
data class PlayerProfileUiState(
    val userId: String = "",
    val playerName: String = "Alejandro Galán",
    val level: Double = 4.85,
    val delta: Double = +0.12,
    val confidenceProgress: Float = 0.92f,
    val winRate: Int = 74,
    val matchesPlayed: Int = 58,
    val bestStreak: Int = 9,
    val currentStreak: Int = 4,
    val preferredSide: String = "REVÉS",
    val recentForm: List<Boolean> = listOf(true, true, true, false, true, true, true, false, true, true),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * MVI / StateFlow ViewModel powering the Player Profile & Stats screen.
 */
class PlayerProfileViewModel(
    private val userRepository: UserRepository,
    initialUserId: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PlayerProfileUiState(
            userId = initialUserId ?: "",
            isLoading = initialUserId != null
        )
    )
    val uiState: StateFlow<PlayerProfileUiState> = _uiState.asStateFlow()

    private var observationJob: Job? = null

    init {
        if (initialUserId != null && initialUserId.isNotEmpty()) {
            loadUser(initialUserId)
        }
    }

    fun loadUser(userId: String) {
        observationJob?.cancel()
        _uiState.value = _uiState.value.copy(
            userId = userId,
            isLoading = true,
            errorMessage = null
        )

        observationJob = viewModelScope.launch {
            try {
                userRepository.observeUserProfile(userId).collect { profile ->
                    if (profile != null) {
                        _uiState.value = _uiState.value.copy(
                            userId = profile.userId,
                            playerName = profile.playerName,
                            level = profile.level,
                            delta = profile.delta,
                            confidenceProgress = profile.confidenceProgress,
                            winRate = profile.winRate,
                            matchesPlayed = profile.matchesPlayed,
                            bestStreak = profile.bestStreak,
                            currentStreak = profile.currentStreak,
                            preferredSide = profile.preferredSide,
                            recentForm = profile.recentForm,
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }
}
