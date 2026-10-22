package com.padelscoring.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ============================================================================
// PADEL UP SHAPES - "AERODYNAMIC COURT GEOMETRY"
// Tactile corner radiuses inspired by glass court panels and perforated racket ergonomics
// ============================================================================

val PadelShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),  // Rank deltas, mini tags
    small = RoundedCornerShape(10.dp),      // Filter chips, badge pills
    medium = RoundedCornerShape(14.dp),     // Quick score pads, court cards
    large = RoundedCornerShape(20.dp),      // Hero cards, tournament command cards
    extraLarge = RoundedCornerShape(28.dp)  // Bottom sheets, modal dialogues
)

// Specific ergonomic shapes
val PadelPillShape = RoundedCornerShape(50)
val PadelScoreButtonShape = RoundedCornerShape(16.dp)
val PadelCourtCardShape = RoundedCornerShape(18.dp)
val PadelSheetTopShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
