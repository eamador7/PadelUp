package com.padelscoring.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for the "users" table storing player profiles and ratings.
 */
@Entity(
    tableName = "users",
    indices = [
        Index("isGuest"),
        Index("createdAt")
    ]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String? = null,
    val avatarUrl: String? = null,
    val level: Double = 2.50,
    val levelConfidence: Double = 0.20,
    val matchesPlayed: Int = 0,
    val matchesWon: Int = 0,
    val preferredSide: String = "BOTH",
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val isGuest: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
