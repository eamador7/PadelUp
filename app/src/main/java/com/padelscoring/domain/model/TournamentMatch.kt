package com.padelscoring.domain.model

/**
 * Domain model representing a single match within a tournament round.
 */
data class TournamentMatch(
    val id: String,
    val roundId: String,
    val tournamentId: String,
    val courtNumber: Int,
    val team1Player1Id: String,
    val team1Player2Id: String,
    val team2Player1Id: String,
    val team2Player2Id: String,
    val team1Score: Int = 0,
    val team2Score: Int = 0,
    val isCompleted: Boolean = false
)
