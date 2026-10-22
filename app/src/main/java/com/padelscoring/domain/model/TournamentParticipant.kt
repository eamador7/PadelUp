package com.padelscoring.domain.model

/**
 * Domain model representing an individual player's standing in a tournament.
 */
data class TournamentParticipant(
    val tournamentId: String,
    val playerId: String,
    val playerName: String,
    val seed: Int = 0,
    val totalPoints: Int = 0,
    val totalDiff: Int = 0,
    val matchesPlayed: Int = 0,
    val matchesWon: Int = 0,
    val rank: Int = 1
)
