package com.padelscoring.data.repository

import com.padelscoring.data.local.dao.MatchDao
import com.padelscoring.data.local.dao.UserDao
import com.padelscoring.data.local.mapper.toDomain
import com.padelscoring.data.local.mapper.toEntity
import com.padelscoring.domain.model.CourtSide
import com.padelscoring.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * UI State representation of a user profile for presentation layer.
 */
data class UserProfileUiState(
    val userId: String,
    val playerName: String,
    val level: Double,
    val delta: Double,
    val confidenceProgress: Float,
    val winRate: Int,
    val matchesPlayed: Int,
    val bestStreak: Int,
    val currentStreak: Int,
    val preferredSide: String,
    val recentForm: List<Boolean>
)

/**
 * Repository interface for user profiles, stats, and 10-match rolling form.
 */
interface UserRepository {
    fun observeUserProfile(userId: String): Flow<UserProfileUiState?>
    fun observeUser(userId: String): Flow<User?>
    suspend fun getUserById(userId: String): User?
    suspend fun insertUser(user: User)
    suspend fun updateUser(user: User)
}

/**
 * Default Room-backed implementation of [UserRepository].
 */
class UserRepositoryImpl(
    private val userDao: UserDao,
    private val matchDao: MatchDao
) : UserRepository {

    override fun observeUserProfile(userId: String): Flow<UserProfileUiState?> {
        return combine(
            userDao.getUserFlow(userId),
            matchDao.getMatchesForUser(userId)
        ) { userEntity, matchEntities ->
            if (userEntity == null) return@combine null

            val user = userEntity.toDomain()
            val matches = matchEntities.map { it.toDomain() }

            // Filter matches where user participated and match is completed
            val userMatches = matches
                .filter { m ->
                    m.team1Player1Id == userId ||
                    m.team1Player2Id == userId ||
                    m.team2Player1Id == userId ||
                    m.team2Player2Id == userId
                }
                .sortedByDescending { it.playedAt }

            // Compute 10-match rolling form: true = win, false = loss
            val rolling10 = userMatches.take(10).map { match ->
                val inTeam1 = match.team1Player1Id == userId || match.team1Player2Id == userId
                val inTeam2 = match.team2Player1Id == userId || match.team2Player2Id == userId
                (inTeam1 && match.winningTeam == 1) || (inTeam2 && match.winningTeam == 2)
            }

            val winRate = if (user.matchesPlayed > 0) {
                ((user.matchesWon.toDouble() / user.matchesPlayed.toDouble()) * 100).toInt()
            } else {
                0
            }

            val preferredSideDisplay = when (user.preferredSide) {
                CourtSide.REVES -> "REVÉS"
                CourtSide.DRIVE -> "DRIVE"
                CourtSide.BOTH -> "BOTH"
            }

            UserProfileUiState(
                userId = user.id,
                playerName = user.name,
                level = user.level,
                delta = 0.0, // Baseline delta
                confidenceProgress = user.levelConfidence.toFloat().coerceIn(0f, 1f),
                winRate = winRate,
                matchesPlayed = user.matchesPlayed,
                bestStreak = user.bestStreak,
                currentStreak = user.currentStreak,
                preferredSide = preferredSideDisplay,
                recentForm = rolling10
            )
        }
    }

    override fun observeUser(userId: String): Flow<User?> {
        return userDao.getUserFlow(userId).map { it?.toDomain() }
    }

    override suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)?.toDomain()
    }

    override suspend fun insertUser(user: User) {
        userDao.insertUser(user.toEntity())
    }

    override suspend fun updateUser(user: User) {
        userDao.updateUser(user.toEntity())
    }
}
