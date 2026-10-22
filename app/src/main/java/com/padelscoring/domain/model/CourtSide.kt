package com.padelscoring.domain.model

/**
 * Represents a padel player's preferred court side:
 * - DRIVE: Right-side player (typically right-handed forehand on the outside).
 * - REVES: Left-side/backhand player (typically controls middle and overheads).
 * - BOTH: Versatile player comfortable on either court side.
 */
enum class CourtSide {
    DRIVE,
    REVES,
    BOTH
}
