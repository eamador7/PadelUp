package com.padelscoring.domain.model

/**
 * Domain model representing a competitive or friendly doubles padel match.
 */
data class Match(
    val id: String,
    val matchType: String = "FRIENDLY",
    val scoringMode: String = "TRADITIONAL",
    val team1Player1Id: String,
    val team1Player2Id: String? = null,
    val team2Player1Id: String,
    val team2Player2Id: String? = null,
    val set1T1: Int,
    val set1T2: Int,
    val set2T1: Int,
    val set2T2: Int,
    val set3T1: Int? = null,
    val set3T2: Int? = null,
    val winningTeam: Int,
    val submittedByUserId: String,
    val confirmationStatus: String = "CONFIRMED",
    val isRanked: Boolean = true,
    val playedAt: Long = System.currentTimeMillis()
)
