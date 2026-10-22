package com.padelscoring.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for the "groups" table storing social groups and squads.
 */
@Entity(
    tableName = "groups",
    indices = [
        Index("createdById"),
        Index("createdAt")
    ]
)
data class GroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String? = null,
    val createdById: String,
    val createdAt: Long = System.currentTimeMillis()
)
