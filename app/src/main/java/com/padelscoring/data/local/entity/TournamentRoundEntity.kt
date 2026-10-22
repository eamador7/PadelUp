package com.padelscoring.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for the "tournament_rounds" table storing round progression in a tournament.
 */
@Entity(
    tableName = "tournament_rounds",
    foreignKeys = [
        ForeignKey(
            entity = TournamentEntity::class,
            parentColumns = ["id"],
            childColumns = ["tournamentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("tournamentId")
    ]
)
data class TournamentRoundEntity(
    @PrimaryKey val id: String,
    val tournamentId: String,
    val roundNumber: Int,
    val status: String = "PENDING"
)
