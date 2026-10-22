package com.padelscoring.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.padelscoring.data.repository.GroupRepository
import com.padelscoring.ui.screens.GroupMemberRanking
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for the Groups Hub & Squad screen.
 */
data class GroupsHubUiState(
    val groupId: String = "",
    val groupName: String = "Friday Night Padel Club",
    val memberCount: Int = 16,
    val members: List<GroupMemberRanking> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * MVI / StateFlow ViewModel powering the Groups Hub and Social Club screen.
 */
class GroupsHubViewModel(
    private val groupRepository: GroupRepository,
    initialGroupId: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        GroupsHubUiState(
            groupId = initialGroupId ?: "",
            isLoading = initialGroupId != null
        )
    )
    val uiState: StateFlow<GroupsHubUiState> = _uiState.asStateFlow()

    private var observationJob: Job? = null

    init {
        if (initialGroupId != null && initialGroupId.isNotEmpty()) {
            loadGroup(initialGroupId)
        }
    }

    fun loadGroup(groupId: String) {
        observationJob?.cancel()
        _uiState.value = _uiState.value.copy(
            groupId = groupId,
            isLoading = true,
            errorMessage = null
        )

        observationJob = viewModelScope.launch {
            try {
                groupRepository.observeGroupLeaderboard(groupId).collect { rankings ->
                    _uiState.value = _uiState.value.copy(
                        groupId = groupId,
                        members = rankings,
                        memberCount = rankings.size,
                        isLoading = false
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
}
