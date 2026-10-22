package com.padelscoring.domain.model

/**
 * Domain model representing a member belonging to a padel social group.
 */
data class GroupMember(
    val groupId: String,
    val userId: String,
    val role: String = "MEMBER",
    val groupElo: Double = 1200.0,
    val groupPoints: Int = 0,
    val tournamentsWon: Int = 0,
    val joinedAt: Long = System.currentTimeMillis()
)
