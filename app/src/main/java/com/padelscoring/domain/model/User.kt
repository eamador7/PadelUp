package com.padelscoring.domain.model

/**
 * Domain model representing a user or guest player in Padel Up.
 */
data class User(
    val id: String,
    val name: String,
    val email: String? = null,
    val avatarUrl: String? = null,
    val level: Double = 2.50,
    val levelConfidence: Double = 0.20,
    val matchesPlayed: Int = 0,
    val matchesWon: Int = 0,
    val preferredSide: CourtSide = CourtSide.BOTH,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val isGuest: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
