package com.padelscoring.domain.model

/**
 * Domain model representing a scheduled round of matches in a tournament.
 */
data class TournamentRound(
    val id: String,
    val tournamentId: String,
    val roundNumber: Int,
    val matches: List<TournamentMatch>
)
