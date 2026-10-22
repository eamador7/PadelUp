package com.padelscoring.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for the "tournaments" table storing Americano and Mexicano tournament metadata.
 */
@Entity(
    tableName = "tournaments",
    indices = [
        Index("groupId"),
        Index("createdAt")
    ]
)
data class TournamentEntity(
    @PrimaryKey val id: String,
    val groupId: String? = null,
    val name: String,
    val format: String = "AMERICANO",
    val targetPoints: Int = 32,
    val totalCourts: Int = 1,
    val status: String = "DRAFT",
    val isOffline: Boolean = false,
    val createdById: String,
    val createdAt: Long = System.currentTimeMillis()
)
