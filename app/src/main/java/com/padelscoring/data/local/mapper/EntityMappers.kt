package com.padelscoring.data.local.mapper

import com.padelscoring.data.local.entity.GroupEntity
import com.padelscoring.data.local.entity.GroupMemberEntity
import com.padelscoring.data.local.entity.MatchConfirmationEntity
import com.padelscoring.data.local.entity.MatchEntity
import com.padelscoring.data.local.entity.TournamentEntity
import com.padelscoring.data.local.entity.TournamentMatchEntity
import com.padelscoring.data.local.entity.TournamentParticipantEntity
import com.padelscoring.data.local.entity.TournamentRoundEntity
import com.padelscoring.data.local.entity.UserEntity
import com.padelscoring.domain.model.CourtSide
import com.padelscoring.domain.model.Group
import com.padelscoring.domain.model.GroupMember
import com.padelscoring.domain.model.Match
import com.padelscoring.domain.model.MatchConfirmation
import com.padelscoring.domain.model.Tournament
import com.padelscoring.domain.model.TournamentFormat
import com.padelscoring.domain.model.TournamentMatch
import com.padelscoring.domain.model.TournamentParticipant
import com.padelscoring.domain.model.TournamentRound
import com.padelscoring.domain.model.TournamentStatus
import com.padelscoring.domain.model.User

// --- User Mappers ---

fun UserEntity.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    avatarUrl = avatarUrl,
    level = level,
    levelConfidence = levelConfidence,
    matchesPlayed = matchesPlayed,
    matchesWon = matchesWon,
    preferredSide = try {
        CourtSide.valueOf(preferredSide)
    } catch (_: Exception) {
        CourtSide.BOTH
    },
    currentStreak = currentStreak,
    bestStreak = bestStreak,
    isGuest = isGuest,
    createdAt = createdAt
)

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    name = name,
    email = email,
    avatarUrl = avatarUrl,
    level = level,
    levelConfidence = levelConfidence,
    matchesPlayed = matchesPlayed,
    matchesWon = matchesWon,
    preferredSide = preferredSide.name,
    currentStreak = currentStreak,
    bestStreak = bestStreak,
    isGuest = isGuest,
    createdAt = createdAt
)

// --- Match Mappers ---

fun MatchEntity.toDomain(): Match = Match(
    id = id,
    matchType = matchType,
    scoringMode = scoringMode,
    team1Player1Id = team1Player1Id,
    team1Player2Id = team1Player2Id,
    team2Player1Id = team2Player1Id,
    team2Player2Id = team2Player2Id,
    set1T1 = set1T1,
    set1T2 = set1T2,
    set2T1 = set2T1,
    set2T2 = set2T2,
    set3T1 = set3T1,
    set3T2 = set3T2,
    winningTeam = winningTeam,
    submittedByUserId = submittedByUserId,
    confirmationStatus = confirmationStatus,
    isRanked = isRanked,
    playedAt = playedAt
)

fun Match.toEntity(): MatchEntity = MatchEntity(
    id = id,
    matchType = matchType,
    scoringMode = scoringMode,
    team1Player1Id = team1Player1Id,
    team1Player2Id = team1Player2Id,
    team2Player1Id = team2Player1Id,
    team2Player2Id = team2Player2Id,
    set1T1 = set1T1,
    set1T2 = set1T2,
    set2T1 = set2T1,
    set2T2 = set2T2,
    set3T1 = set3T1,
    set3T2 = set3T2,
    winningTeam = winningTeam,
    submittedByUserId = submittedByUserId,
    confirmationStatus = confirmationStatus,
    isRanked = isRanked,
    playedAt = playedAt
)

// --- Match Confirmation Mappers ---

fun MatchConfirmationEntity.toDomain(): MatchConfirmation = MatchConfirmation(
    id = id,
    matchId = matchId,
    userId = userId,
    status = status,
    confirmedAt = confirmedAt,
    disputeReason = disputeReason
)

fun MatchConfirmation.toEntity(): MatchConfirmationEntity = MatchConfirmationEntity(
    id = id,
    matchId = matchId,
    userId = userId,
    status = status,
    confirmedAt = confirmedAt,
    disputeReason = disputeReason
)

// --- Tournament Mappers ---

fun TournamentEntity.toDomain(): Tournament = Tournament(
    id = id,
    groupId = groupId,
    name = name,
    format = try {
        TournamentFormat.valueOf(format)
    } catch (_: Exception) {
        TournamentFormat.AMERICANO
    },
    targetPoints = targetPoints,
    totalCourts = totalCourts,
    status = try {
        TournamentStatus.valueOf(status)
    } catch (_: Exception) {
        TournamentStatus.DRAFT
    },
    isOffline = isOffline,
    createdById = createdById,
    createdAt = createdAt
)

fun Tournament.toEntity(): TournamentEntity = TournamentEntity(
    id = id,
    groupId = groupId,
    name = name,
    format = format.name,
    targetPoints = targetPoints,
    totalCourts = totalCourts,
    status = status.name,
    isOffline = isOffline,
    createdById = createdById,
    createdAt = createdAt
)

// --- Tournament Participant Mappers ---

fun TournamentParticipantEntity.toDomain(): TournamentParticipant = TournamentParticipant(
    tournamentId = tournamentId,
    playerId = playerId,
    playerName = playerName,
    seed = seed,
    totalPoints = totalPoints,
    totalDiff = totalDiff,
    matchesPlayed = matchesPlayed,
    matchesWon = matchesWon,
    rank = rank
)

fun TournamentParticipant.toEntity(): TournamentParticipantEntity = TournamentParticipantEntity(
    tournamentId = tournamentId,
    playerId = playerId,
    playerName = playerName,
    seed = seed,
    totalPoints = totalPoints,
    totalDiff = totalDiff,
    matchesPlayed = matchesPlayed,
    matchesWon = matchesWon,
    rank = rank
)

// --- Tournament Round Mappers ---

fun TournamentRoundEntity.toDomain(matches: List<TournamentMatch> = emptyList()): TournamentRound = TournamentRound(
    id = id,
    tournamentId = tournamentId,
    roundNumber = roundNumber,
    matches = matches
)

fun TournamentRound.toEntity(status: String = "PENDING"): TournamentRoundEntity = TournamentRoundEntity(
    id = id,
    tournamentId = tournamentId,
    roundNumber = roundNumber,
    status = status
)

// --- Tournament Match Mappers ---

fun TournamentMatchEntity.toDomain(): TournamentMatch = TournamentMatch(
    id = id,
    roundId = roundId,
    tournamentId = tournamentId,
    courtNumber = courtNumber,
    team1Player1Id = team1Player1Id,
    team1Player2Id = team1Player2Id,
    team2Player1Id = team2Player1Id,
    team2Player2Id = team2Player2Id,
    team1Score = team1Score,
    team2Score = team2Score,
    isCompleted = isCompleted
)

fun TournamentMatch.toEntity(): TournamentMatchEntity = TournamentMatchEntity(
    id = id,
    roundId = roundId,
    tournamentId = tournamentId,
    courtNumber = courtNumber,
    team1Player1Id = team1Player1Id,
    team1Player2Id = team1Player2Id,
    team2Player1Id = team2Player1Id,
    team2Player2Id = team2Player2Id,
    team1Score = team1Score,
    team2Score = team2Score,
    isCompleted = isCompleted
)

// --- Group & Member Mappers ---

fun GroupEntity.toDomain(): Group = Group(
    id = id,
    name = name,
    description = description,
    createdById = createdById,
    createdAt = createdAt
)

fun Group.toEntity(): GroupEntity = GroupEntity(
    id = id,
    name = name,
    description = description,
    createdById = createdById,
    createdAt = createdAt
)

fun GroupMemberEntity.toDomain(): GroupMember = GroupMember(
    groupId = groupId,
    userId = userId,
    role = role,
    groupElo = groupElo,
    groupPoints = groupPoints,
    tournamentsWon = tournamentsWon,
    joinedAt = joinedAt
)

fun GroupMember.toEntity(): GroupMemberEntity = GroupMemberEntity(
    groupId = groupId,
    userId = userId,
    role = role,
    groupElo = groupElo,
    groupPoints = groupPoints,
    tournamentsWon = tournamentsWon,
    joinedAt = joinedAt
)
