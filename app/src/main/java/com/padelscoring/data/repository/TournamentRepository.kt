package com.padelscoring.data.repository

import com.padelscoring.data.local.dao.TournamentDao
import com.padelscoring.data.local.entity.TournamentEntity
import com.padelscoring.data.local.entity.TournamentParticipantEntity
import com.padelscoring.data.local.entity.TournamentRoundEntity
import com.padelscoring.data.local.mapper.toDomain
import com.padelscoring.data.local.mapper.toEntity
import com.padelscoring.domain.model.Tournament
import com.padelscoring.domain.model.TournamentFormat
import com.padelscoring.domain.model.TournamentMatch
import com.padelscoring.domain.model.TournamentParticipant
import com.padelscoring.domain.model.TournamentRound
import com.padelscoring.domain.model.TournamentStatus
import com.padelscoring.domain.tournament.AmericanoEngine
import com.padelscoring.domain.tournament.MexicanoEngine
import com.padelscoring.ui.screens.AmericanoPlayerStanding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * UI State representation of a tournament for presentation layer.
 */
data class CourtMatchUiState(
    val matchId: String,
    val courtNumber: Int,
    val teamANames: Pair<String, String>,
    val teamBNames: Pair<String, String>,
    val teamAScore: Int,
    val teamBScore: Int,
    val isCompleted: Boolean
)

data class TournamentUiState(
    val tournament: Tournament? = null,
    val currentRoundNumber: Int = 1,
    val totalRounds: Int = 1,
    val targetPoints: Int = 32,
    val courtMatches: List<CourtMatchUiState> = emptyList(),
    val standings: List<AmericanoPlayerStanding> = emptyList(),
    val canGenerateNextRound: Boolean = false,
    val isFinished: Boolean = false,
    val isLoading: Boolean = false
)

/**
 * Repository interface for tournament persistence, scheduling, and live scoring.
 */
interface TournamentRepository {
    suspend fun createAmericanoTournament(
        name: String,
        playerNames: List<String>,
        targetPoints: Int = 32,
        totalCourts: Int = playerNames.size / 4,
        isOffline: Boolean = true
    ): String

    suspend fun createMexicanoTournament(
        name: String,
        playerNames: List<String>,
        targetPoints: Int = 32,
        totalCourts: Int = playerNames.size / 4,
        isOffline: Boolean = true,
        totalRounds: Int = playerNames.size - 1
    ): String

    fun observeTournament(tournamentId: String): Flow<TournamentUiState>
    suspend fun recordMatchScore(matchId: String, t1Score: Int, t2Score: Int)
    suspend fun advanceToNextRound(tournamentId: String)
}

/**
 * Default Room-backed implementation of [TournamentRepository].
 */
class TournamentRepositoryImpl(
    private val tournamentDao: TournamentDao
) : TournamentRepository {

    override suspend fun createAmericanoTournament(
        name: String,
        playerNames: List<String>,
        targetPoints: Int,
        totalCourts: Int,
        isOffline: Boolean
    ): String {
        require(playerNames.size >= 4) { "Americano requires at least 4 players (received ${playerNames.size})" }
        require(totalCourts >= 1) { "Total courts must be at least 1 (received $totalCourts)" }

        val tournamentId = UUID.randomUUID().toString()
        val tournamentEntity = TournamentEntity(
            id = tournamentId,
            groupId = null,
            name = name,
            format = TournamentFormat.AMERICANO.name,
            targetPoints = targetPoints,
            totalCourts = totalCourts,
            status = TournamentStatus.IN_PROGRESS.name,
            isOffline = isOffline,
            createdById = "offline-user",
            createdAt = System.currentTimeMillis()
        )

        val participantEntities = playerNames.mapIndexed { index, playerName ->
            TournamentParticipantEntity(
                tournamentId = tournamentId,
                playerId = UUID.randomUUID().toString(),
                playerName = playerName,
                seed = index + 1,
                totalPoints = 0,
                totalDiff = 0,
                matchesPlayed = 0,
                matchesWon = 0,
                rank = index + 1
            )
        }

        val playerIds = participantEntities.map { it.playerId }
        val generatedRounds = AmericanoEngine.generateSchedule(
            playerIds = playerIds,
            totalCourts = totalCourts,
            tournamentId = tournamentId
        )

        val roundEntities = generatedRounds.mapIndexed { index, round ->
            TournamentRoundEntity(
                id = round.id,
                tournamentId = tournamentId,
                roundNumber = round.roundNumber,
                status = if (index == 0) "ACTIVE" else "PENDING"
            )
        }

        val matchEntities = generatedRounds.flatMap { round ->
            round.matches.map { it.toEntity() }
        }

        tournamentDao.insertCompleteTournament(
            tournament = tournamentEntity,
            participants = participantEntities,
            rounds = roundEntities,
            matches = matchEntities
        )

        return tournamentId
    }

    override suspend fun createMexicanoTournament(
        name: String,
        playerNames: List<String>,
        targetPoints: Int,
        totalCourts: Int,
        isOffline: Boolean,
        totalRounds: Int
    ): String {
        require(playerNames.size >= 4) { "Mexicano requires at least 4 players (received ${playerNames.size})" }
        require(totalCourts >= 1) { "Total courts must be at least 1 (received $totalCourts)" }

        val tournamentId = UUID.randomUUID().toString()
        val tournamentEntity = TournamentEntity(
            id = tournamentId,
            groupId = null,
            name = name,
            format = TournamentFormat.MEXICANO.name,
            targetPoints = targetPoints,
            totalCourts = totalCourts,
            status = TournamentStatus.IN_PROGRESS.name,
            isOffline = isOffline,
            createdById = "offline-user",
            createdAt = System.currentTimeMillis()
        )

        val participantEntities = playerNames.mapIndexed { index, playerName ->
            TournamentParticipantEntity(
                tournamentId = tournamentId,
                playerId = UUID.randomUUID().toString(),
                playerName = playerName,
                seed = index + 1,
                totalPoints = 0,
                totalDiff = 0,
                matchesPlayed = 0,
                matchesWon = 0,
                rank = index + 1
            )
        }

        val participants = participantEntities.map { it.toDomain() }
        val round1 = MexicanoEngine.generateNextRound(
            tournamentId = tournamentId,
            roundNumber = 1,
            rankedParticipants = participants,
            pastRounds = emptyList(),
            totalCourts = totalCourts
        )

        tournamentDao.insertCompleteTournament(
            tournament = tournamentEntity,
            participants = participantEntities,
            rounds = listOf(round1.toEntity(status = "ACTIVE")),
            matches = round1.matches.map { it.toEntity() }
        )

        return tournamentId
    }

    override fun observeTournament(tournamentId: String): Flow<TournamentUiState> {
        return combine(
            tournamentDao.getTournamentFlow(tournamentId),
            tournamentDao.getRoundsFlow(tournamentId),
            tournamentDao.getTournamentMatchesFlow(tournamentId),
            tournamentDao.getParticipantsFlow(tournamentId)
        ) { tournamentEntity, roundEntities, matchEntities, participantEntities ->
            if (tournamentEntity == null) {
                return@combine TournamentUiState(isLoading = false)
            }

            val tournament = tournamentEntity.toDomain()
            val participants = participantEntities.map { it.toDomain() }
            val allMatches = matchEntities.map { it.toDomain() }
            val rounds = roundEntities.map { r ->
                r.toDomain(allMatches.filter { it.roundId == r.id })
            }

            // Find current active round
            val activeRound = roundEntities.firstOrNull { it.status == "ACTIVE" }
                ?: roundEntities.firstOrNull { it.status != "COMPLETED" }
                ?: roundEntities.lastOrNull()

            val activeMatches = if (activeRound != null) {
                allMatches.filter { it.roundId == activeRound.id }
            } else {
                emptyList()
            }

            val playerNameMap = participants.associate { it.playerId to it.playerName }

            val courtMatchesUi = activeMatches.map { match ->
                CourtMatchUiState(
                    matchId = match.id,
                    courtNumber = match.courtNumber,
                    teamANames = Pair(
                        playerNameMap[match.team1Player1Id] ?: "Player",
                        playerNameMap[match.team1Player2Id] ?: "Player"
                    ),
                    teamBNames = Pair(
                        playerNameMap[match.team2Player1Id] ?: "Player",
                        playerNameMap[match.team2Player2Id] ?: "Player"
                    ),
                    teamAScore = match.team1Score,
                    teamBScore = match.team2Score,
                    isCompleted = match.isCompleted
                )
            }

            val standingsUi = participants.map { p ->
                AmericanoPlayerStanding(
                    id = p.playerId,
                    name = p.playerName,
                    rank = p.rank,
                    rankDelta = p.seed - p.rank,
                    played = p.matchesPlayed,
                    won = p.matchesWon,
                    pointDiff = p.totalDiff,
                    totalPoints = p.totalPoints
                )
            }

            val allActiveMatchesCompleted = activeMatches.isNotEmpty() && activeMatches.all { it.isCompleted }
            val currentRoundNumber = activeRound?.roundNumber ?: 1
            val totalRounds = if (tournament.format == TournamentFormat.AMERICANO) {
                rounds.size.coerceAtLeast(1)
            } else {
                // For Mexicano, total rounds defaults to N - 1
                (participants.size - 1).coerceAtLeast(1)
            }

            val isLastRound = currentRoundNumber >= totalRounds
            val isFinished = tournament.status == TournamentStatus.COMPLETED || (isLastRound && allActiveMatchesCompleted)

            val canGenerateNextRound = allActiveMatchesCompleted && !isFinished

            TournamentUiState(
                tournament = tournament,
                currentRoundNumber = currentRoundNumber,
                totalRounds = totalRounds,
                targetPoints = tournament.targetPoints,
                courtMatches = courtMatchesUi,
                standings = standingsUi,
                canGenerateNextRound = canGenerateNextRound,
                isFinished = isFinished,
                isLoading = false
            )
        }
    }

    override suspend fun recordMatchScore(matchId: String, t1Score: Int, t2Score: Int) {
        require(t1Score >= 0 && t2Score >= 0) { "Scores must be non-negative" }
        val matchEntity = tournamentDao.getMatchById(matchId)
            ?: throw IllegalArgumentException("Match with id $matchId not found")

        val tournamentEntity = tournamentDao.getTournamentById(matchEntity.tournamentId)
            ?: throw IllegalArgumentException("Tournament with id ${matchEntity.tournamentId} not found")

        val targetPoints = tournamentEntity.targetPoints
        require(t1Score + t2Score == targetPoints) {
            "Total points ($t1Score + $t2Score = ${t1Score + t2Score}) must equal target points ($targetPoints)"
        }

        // Update match in SQLite
        tournamentDao.updateMatchScore(matchId, t1Score, t2Score)

        // Recalculate leaderboard
        val participants = tournamentDao.getParticipants(tournamentEntity.id).map { it.toDomain() }
        val allMatches = tournamentDao.getTournamentMatches(tournamentEntity.id).map { it.toDomain() }

        val recalculated = AmericanoEngine.calculateLeaderboard(participants, allMatches)
        tournamentDao.insertParticipants(recalculated.map { it.toEntity() })
    }

    override suspend fun advanceToNextRound(tournamentId: String) {
        val tournamentEntity = tournamentDao.getTournamentById(tournamentId)
            ?: throw IllegalArgumentException("Tournament with id $tournamentId not found")

        val rounds = tournamentDao.getRounds(tournamentId)
        val activeRound = rounds.firstOrNull { it.status == "ACTIVE" }
            ?: rounds.firstOrNull { it.status != "COMPLETED" }
            ?: return

        // Mark active round completed
        tournamentDao.updateRoundStatus(activeRound.id, "COMPLETED")

        if (tournamentEntity.format == TournamentFormat.MEXICANO.name) {
            val nextRoundNumber = activeRound.roundNumber + 1
            val participants = tournamentDao.getParticipants(tournamentId).map { it.toDomain() }
            val allMatches = tournamentDao.getTournamentMatches(tournamentId).map { it.toDomain() }

            val ranked = AmericanoEngine.calculateLeaderboard(participants, allMatches)
            val pastRounds = rounds.map { r ->
                r.toDomain(allMatches.filter { it.roundId == r.id })
            }

            val nextRound = MexicanoEngine.generateNextRound(
                tournamentId = tournamentId,
                roundNumber = nextRoundNumber,
                rankedParticipants = ranked,
                pastRounds = pastRounds,
                totalCourts = tournamentEntity.totalCourts
            )

            tournamentDao.insertRounds(listOf(nextRound.toEntity(status = "ACTIVE")))
            tournamentDao.insertMatches(nextRound.matches.map { it.toEntity() })
        } else {
            // AMERICANO
            val nextRound = rounds.firstOrNull { it.roundNumber == activeRound.roundNumber + 1 }
            if (nextRound != null) {
                tournamentDao.updateRoundStatus(nextRound.id, "ACTIVE")
            } else {
                tournamentDao.updateTournamentStatus(tournamentId, TournamentStatus.COMPLETED.name)
            }
        }
    }
}
