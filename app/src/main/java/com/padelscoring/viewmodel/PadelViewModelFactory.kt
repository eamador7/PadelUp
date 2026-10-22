package com.padelscoring.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.padelscoring.data.local.PadelDatabase
import com.padelscoring.data.repository.GroupRepository
import com.padelscoring.data.repository.GroupRepositoryImpl
import com.padelscoring.data.repository.MatchRepository
import com.padelscoring.data.repository.MatchRepositoryImpl
import com.padelscoring.data.repository.TournamentRepository
import com.padelscoring.data.repository.TournamentRepositoryImpl
import com.padelscoring.data.repository.UserRepository
import com.padelscoring.data.repository.UserRepositoryImpl

/**
 * ViewModelProvider Factory for providing repositories to Padel Up ViewModels.
 */
class PadelViewModelFactory(
    private val tournamentRepository: TournamentRepository? = null,
    private val matchRepository: MatchRepository? = null,
    private val userRepository: UserRepository? = null,
    private val groupRepository: GroupRepository? = null,
    private val initialId: String? = null
) : ViewModelProvider.Factory {

    companion object {
        fun fromDatabase(
            database: PadelDatabase,
            initialId: String? = null
        ): PadelViewModelFactory {
            val tournamentRepo = TournamentRepositoryImpl(database.tournamentDao())
            val matchRepo = MatchRepositoryImpl(database.matchDao(), database.userDao())
            val userRepo = UserRepositoryImpl(database.userDao(), database.matchDao())
            val groupRepo = GroupRepositoryImpl(database.groupDao(), database.userDao())
            return PadelViewModelFactory(
                tournamentRepository = tournamentRepo,
                matchRepository = matchRepo,
                userRepository = userRepo,
                groupRepository = groupRepo,
                initialId = initialId
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AmericanoTournamentViewModel::class.java) -> {
                val repo = tournamentRepository ?: error("TournamentRepository required")
                AmericanoTournamentViewModel(repo, initialId) as T
            }
            modelClass.isAssignableFrom(MatchConfirmationViewModel::class.java) -> {
                val repo = matchRepository ?: error("MatchRepository required")
                MatchConfirmationViewModel(repo, userRepository, initialId) as T
            }
            modelClass.isAssignableFrom(OfflineLauncherViewModel::class.java) -> {
                val repo = tournamentRepository ?: error("TournamentRepository required")
                OfflineLauncherViewModel(repo) as T
            }
            modelClass.isAssignableFrom(PlayerProfileViewModel::class.java) -> {
                val repo = userRepository ?: error("UserRepository required")
                PlayerProfileViewModel(repo, initialId) as T
            }
            modelClass.isAssignableFrom(GroupsHubViewModel::class.java) -> {
                val repo = groupRepository ?: error("GroupRepository required")
                GroupsHubViewModel(repo, initialId) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
