package com.padelscoring.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for the "tournament_matches" table storing individual matches within tournament rounds.
 */
@Entity(
    tableName = "tournament_matches",
    foreignKeys = [
        ForeignKey(
            entity = TournamentRoundEntity::class,
            parentColumns = ["id"],
            childColumns = ["roundId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("roundId"),
        Index("tournamentId")
    ]
)
data class TournamentMatchEntity(
    @PrimaryKey val id: String,
    val roundId: String,
    val tournamentId: String,
    val courtNumber: Int,
    val team1Player1Id: String,
    val team1Player2Id: String,
    val team2Player1Id: String,
    val team2Player2Id: String,
    val team1Score: Int = 0,
    val team2Score: Int = 0,
    val isCompleted: Boolean = false
)
