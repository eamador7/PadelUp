package com.padelscoring.domain.model

/**
 * Domain model representing a match confirmation or dispute submitted by a participant.
 */
data class MatchConfirmation(
    val id: String,
    val matchId: String,
    val userId: String,
    val status: String,
    val confirmedAt: Long? = null,
    val disputeReason: String? = null
)
