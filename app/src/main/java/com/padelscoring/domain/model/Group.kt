package com.padelscoring.domain.model

/**
 * Domain model representing a padel social group or club squad.
 */
data class Group(
    val id: String,
    val name: String,
    val description: String? = null,
    val createdById: String,
    val createdAt: Long = System.currentTimeMillis()
)
