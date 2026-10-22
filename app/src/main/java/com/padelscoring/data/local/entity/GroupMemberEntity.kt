package com.padelscoring.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Room entity for the "group_members" join table storing memberships, roles, and group elo.
 */
@Entity(
    tableName = "group_members",
    primaryKeys = ["groupId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("groupId"),
        Index("userId")
    ]
)
data class GroupMemberEntity(
    val groupId: String,
    val userId: String,
    val role: String = "MEMBER",
    val groupElo: Double = 1200.0,
    val groupPoints: Int = 0,
    val tournamentsWon: Int = 0,
    val joinedAt: Long = System.currentTimeMillis()
)
