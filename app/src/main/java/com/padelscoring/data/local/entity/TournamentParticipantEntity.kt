package com.padelscoring.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Room entity for the "tournament_participants" table storing participant standings within a tournament.
 */
@Entity(
    tableName = "tournament_participants",
    primaryKeys = ["tournamentId", "playerId"],
    foreignKeys = [
        ForeignKey(
            entity = TournamentEntity::class,
            parentColumns = ["id"],
            childColumns = ["tournamentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("tournamentId"),
        Index("playerId")
    ]
)
data class TournamentParticipantEntity(
    val tournamentId: String,
    val playerId: String,
    val playerName: String,
    val seed: Int = 0,
    val totalPoints: Int = 0,
    val totalDiff: Int = 0,
    val matchesPlayed: Int = 0,
    val matchesWon: Int = 0,
    val rank: Int = 1
)
