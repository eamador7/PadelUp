package com.padelscoring.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for the "match_confirmations" table storing match confirmations and disputes.
 */
@Entity(
    tableName = "match_confirmations",
    foreignKeys = [
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["matchId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("matchId"),
        Index("userId")
    ]
)
data class MatchConfirmationEntity(
    @PrimaryKey val id: String,
    val matchId: String,
    val userId: String,
    val status: String,
    val confirmedAt: Long? = null,
    val disputeReason: String? = null
)
