package com.padelscoring.data.repository

import com.padelscoring.data.local.dao.MatchDao
import com.padelscoring.data.local.dao.UserDao
import com.padelscoring.data.local.entity.MatchConfirmationEntity
import com.padelscoring.data.local.mapper.toDomain
import com.padelscoring.data.local.mapper.toEntity
import com.padelscoring.domain.model.CourtSide
import com.padelscoring.domain.model.Match
import com.padelscoring.domain.model.User
import com.padelscoring.domain.rating.PadelDynamicRatingCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Repository interface for match submission, confirmation verification, and PDR skill rating evolution.
 */
interface MatchRepository {
    suspend fun submitMatch(match: Match)
    suspend fun confirmMatch(matchId: String, userId: String)
    suspend fun disputeMatch(matchId: String, userId: String, reason: String)
    fun observePendingMatches(): Flow<List<Match>>
    fun observeMatch(matchId: String): Flow<Match?>
    suspend fun getMatch(matchId: String): Match?
}

/**
 * Default Room-backed implementation of [MatchRepository].
 */
class MatchRepositoryImpl(
    private val matchDao: MatchDao,
    private val userDao: UserDao
) : MatchRepository {

    override suspend fun submitMatch(match: Match) {
        matchDao.insertMatch(match.toEntity())
        val confirmation = MatchConfirmationEntity(
            id = UUID.randomUUID().toString(),
            matchId = match.id,
            userId = match.submittedByUserId,
            status = match.confirmationStatus,
            confirmedAt = if (match.confirmationStatus == "CONFIRMED") System.currentTimeMillis() else null,
            disputeReason = null
        )
        matchDao.insertConfirmation(confirmation)
    }

    override suspend fun confirmMatch(matchId: String, userId: String) {
        val matchEntity = matchDao.getMatchById(matchId)
            ?: throw IllegalArgumentException("Match with id $matchId not found")
        val match = matchEntity.toDomain()

        // Guard against duplicate confirmation to prevent double rating adjustments
        if (match.confirmationStatus == "CONFIRMED") {
            return
        }

        // Transition match confirmation status to CONFIRMED
        matchDao.updateConfirmationStatus(matchId, "CONFIRMED")

        // Record participant confirmation
        val existingConf = matchDao.getConfirmationForUser(matchId, userId)
        val confId = existingConf?.id ?: UUID.randomUUID().toString()
        matchDao.insertConfirmation(
            MatchConfirmationEntity(
                id = confId,
                matchId = matchId,
                userId = userId,
                status = "CONFIRMED",
                confirmedAt = System.currentTimeMillis(),
                disputeReason = null
            )
        )

        // If match is ranked and doubles (all 4 players present), compute Playtomic Dynamic Rating (PDR)
        if (match.isRanked) {
            val p1Id = match.team1Player1Id
            val p2Id = match.team1Player2Id
            val p3Id = match.team2Player1Id
            val p4Id = match.team2Player2Id

            if (p2Id != null && p4Id != null) {
                val u1 = getOrCreateUser(p1Id)
                val u2 = getOrCreateUser(p2Id)
                val u3 = getOrCreateUser(p3Id)
                val u4 = getOrCreateUser(p4Id)

                val team1Games = match.set1T1 + match.set2T1 + (match.set3T1 ?: 0)
                val team2Games = match.set1T2 + match.set2T2 + (match.set3T2 ?: 0)

                val ratingResults = PadelDynamicRatingCalculator.calculateMatchRating(
                    team1Player1 = PadelDynamicRatingCalculator.PlayerRating(
                        userId = u1.id,
                        level = u1.level,
                        confidence = u1.levelConfidence
                    ),
                    team1Player2 = PadelDynamicRatingCalculator.PlayerRating(
                        userId = u2.id,
                        level = u2.level,
                        confidence = u2.levelConfidence
                    ),
                    team2Player1 = PadelDynamicRatingCalculator.PlayerRating(
                        userId = u3.id,
                        level = u3.level,
                        confidence = u3.levelConfidence
                    ),
                    team2Player2 = PadelDynamicRatingCalculator.PlayerRating(
                        userId = u4.id,
                        level = u4.level,
                        confidence = u4.levelConfidence
                    ),
                    team1GamesWon = team1Games,
                    team2GamesWon = team2Games,
                    winningTeam = match.winningTeam
                )

                val players = listOf(u1, u2, u3, u4)
                for (u in players) {
                    val result = ratingResults[u.id] ?: continue
                    val won = (match.winningTeam == 1 && (u.id == p1Id || u.id == p2Id)) ||
                            (match.winningTeam == 2 && (u.id == p3Id || u.id == p4Id))

                    val newCurrentStreak = if (won) u.currentStreak + 1 else 0
                    val newBestStreak = maxOf(u.bestStreak, newCurrentStreak)

                    val updatedUser = u.copy(
                        level = result.newLevel,
                        levelConfidence = result.newConfidence,
                        matchesPlayed = u.matchesPlayed + 1,
                        matchesWon = if (won) u.matchesWon + 1 else u.matchesWon,
                        currentStreak = newCurrentStreak,
                        bestStreak = newBestStreak
                    )
                    userDao.updateUser(updatedUser.toEntity())
                }
            }
        }
    }

    override suspend fun disputeMatch(matchId: String, userId: String, reason: String) {
        val matchEntity = matchDao.getMatchById(matchId)
            ?: throw IllegalArgumentException("Match with id $matchId not found")

        matchDao.updateConfirmationStatus(matchId, "DISPUTED")

        val existingConf = matchDao.getConfirmationForUser(matchId, userId)
        val confId = existingConf?.id ?: UUID.randomUUID().toString()
        matchDao.insertConfirmation(
            MatchConfirmationEntity(
                id = confId,
                matchId = matchId,
                userId = userId,
                status = "DISPUTED",
                confirmedAt = System.currentTimeMillis(),
                disputeReason = reason
            )
        )
    }

    override fun observePendingMatches(): Flow<List<Match>> {
        return matchDao.getPendingMatches().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeMatch(matchId: String): Flow<Match?> {
        return matchDao.getMatchFlow(matchId).map { it?.toDomain() }
    }

    override suspend fun getMatch(matchId: String): Match? {
        return matchDao.getMatchById(matchId)?.toDomain()
    }

    private suspend fun getOrCreateUser(userId: String): User {
        val existing = userDao.getUserById(userId)
        return if (existing != null) {
            existing.toDomain()
        } else {
            val defaultUser = User(
                id = userId,
                name = "Player $userId",
                level = 2.50,
                levelConfidence = 0.20,
                preferredSide = CourtSide.BOTH,
                isGuest = false
            )
            userDao.insertUser(defaultUser.toEntity())
            defaultUser
        }
    }
}
