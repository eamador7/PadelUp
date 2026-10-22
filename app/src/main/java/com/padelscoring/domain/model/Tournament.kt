package com.padelscoring.domain.model

/**
 * Domain model representing an Americano or Mexicano padel tournament.
 */
data class Tournament(
    val id: String,
    val groupId: String? = null,
    val name: String,
    val format: TournamentFormat = TournamentFormat.AMERICANO,
    val targetPoints: Int = 32,
    val totalCourts: Int,
    val status: TournamentStatus = TournamentStatus.DRAFT,
    val isOffline: Boolean = false,
    val createdById: String,
    val createdAt: Long = System.currentTimeMillis()
)
