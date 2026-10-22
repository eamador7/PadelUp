package com.padelscoring.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.padelscoring.data.repository.MatchRepository
import com.padelscoring.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for the Match Confirmation screen.
 */
data class ConfirmationUiState(
    val matchId: String = "",
    val courtNumber: Int = 1,
    val roundNumber: Int = 1,
    val teamANames: Pair<String, String> = Pair("Ale Galán", "Arturo Coello"),
    val teamBNames: Pair<String, String> = Pair("Juan Lebrón", "Agustín Tapia"),
    val finalScoreA: Int = 18,
    val finalScoreB: Int = 14,
    val status: String = "PENDING",
    val isConfirmed: Boolean = false,
    val isDisputed: Boolean = false,
    val disputeReason: String? = null,
    val qrCodePayload: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * MVI / StateFlow ViewModel powering the Match Confirmation & Verification screen.
 */
class MatchConfirmationViewModel(
    private val matchRepository: MatchRepository,
    private val userRepository: UserRepository? = null,
    initialMatchId: String? = null,
    private val currentUserId: String = "current-user"
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ConfirmationUiState(
            matchId = initialMatchId ?: "",
            isLoading = initialMatchId != null
        )
    )
    val uiState: StateFlow<ConfirmationUiState> = _uiState.asStateFlow()

    private var observationJob: Job? = null

    init {
        if (initialMatchId != null && initialMatchId.isNotEmpty()) {
            loadMatch(initialMatchId)
        }
    }

    /**
     * Connects this ViewModel to observe a specific match from repository.
     */
    fun loadMatch(matchId: String) {
        observationJob?.cancel()
        _uiState.value = _uiState.value.copy(
            matchId = matchId,
            isLoading = true,
            errorMessage = null
        )

        observationJob = viewModelScope.launch {
            try {
                matchRepository.observeMatch(matchId).collect { match ->
                    if (match != null) {
                        val scoreA = match.set1T1 + match.set2T1 + (match.set3T1 ?: 0)
                        val scoreB = match.set1T2 + match.set2T2 + (match.set3T2 ?: 0)

                        val p1Name = userRepository?.getUserById(match.team1Player1Id)?.name ?: match.team1Player1Id
                        val p2Name = match.team1Player2Id?.let { userRepository?.getUserById(it)?.name ?: it } ?: "Partner A"
                        val p3Name = userRepository?.getUserById(match.team2Player1Id)?.name ?: match.team2Player1Id
                        val p4Name = match.team2Player2Id?.let { userRepository?.getUserById(it)?.name ?: it } ?: "Partner B"

                        _uiState.value = _uiState.value.copy(
                            matchId = match.id,
                            courtNumber = 1,
                            roundNumber = 1,
                            teamANames = Pair(p1Name, p2Name),
                            teamBNames = Pair(p3Name, p4Name),
                            finalScoreA = scoreA,
                            finalScoreB = scoreB,
                            status = match.confirmationStatus,
                            isConfirmed = match.confirmationStatus == "CONFIRMED",
                            isDisputed = match.confirmationStatus == "DISPUTED",
                            qrCodePayload = "padelup://match/${match.id}",
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

    /**
     * Verifies the match result and triggers Playtomic Dynamic Rating (PDR) progression.
     */
    fun onSlideToConfirm() {
        val matchId = _uiState.value.matchId
        if (matchId.isEmpty()) return

        viewModelScope.launch {
            try {
                matchRepository.confirmMatch(matchId, currentUserId)
                _uiState.value = _uiState.value.copy(
                    status = "CONFIRMED",
                    isConfirmed = true,
                    isDisputed = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    /**
     * Flags this match as disputed with an explanation reason.
     */
    fun onDispute(reason: String) {
        val matchId = _uiState.value.matchId
        if (matchId.isEmpty()) return

        viewModelScope.launch {
            try {
                matchRepository.disputeMatch(matchId, currentUserId, reason)
                _uiState.value = _uiState.value.copy(
                    status = "DISPUTED",
                    isDisputed = true,
                    isConfirmed = false,
                    disputeReason = reason
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    /**
     * Handles QR code scan match confirmation.
     */
    fun onQrScanConfirmed(qrData: String) {
        val targetMatchId = if (qrData.startsWith("padelup://match/")) {
            qrData.substringAfter("padelup://match/")
        } else {
            qrData
        }

        viewModelScope.launch {
            try {
                matchRepository.confirmMatch(targetMatchId, currentUserId)
                if (targetMatchId == _uiState.value.matchId) {
                    _uiState.value = _uiState.value.copy(
                        status = "CONFIRMED",
                        isConfirmed = true,
                        isDisputed = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }
}
