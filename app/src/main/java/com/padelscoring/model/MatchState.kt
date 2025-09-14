package com.padelscoring.model

data class MatchState(
    val teamA: TeamState = TeamState(),
    val teamB: TeamState = TeamState(),
    val scoringMode: ScoringMode = ScoringMode.BEST_OF_THREE_TRADITIONAL,
    val server: ServingTeam = ServingTeam.TEAM_A,
    val isTieBreakActive: Boolean = false,
    val isMatchOver: Boolean = false,
    val winner: ServingTeam? = null
)
