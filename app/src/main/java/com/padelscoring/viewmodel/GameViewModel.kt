package com.padelscoring.viewmodel

import androidx.lifecycle.ViewModel
import com.padelscoring.model.MatchState
import com.padelscoring.model.ScoringMode
import com.padelscoring.model.ServingTeam
import com.padelscoring.model.TeamState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel : ViewModel() {

    private val _matchState = MutableStateFlow(MatchState())
    private val history = mutableListOf<MatchState>()
    val matchState: StateFlow<MatchState> = _matchState.asStateFlow()

    fun addPoint(team: ServingTeam) {
        history.add(_matchState.value)
        _matchState.value = calculateNewState(team)
    }

    private fun calculateNewState(team: ServingTeam): MatchState {
        val currentState = _matchState.value
        if (currentState.isTieBreakActive) {
            return addTieBreakPoint(team, currentState)
        }

        val (scorer, other) = if (team == ServingTeam.TEAM_A) {
            currentState.teamA to currentState.teamB
        } else {
            currentState.teamB to currentState.teamA
        }

        return when (scorer.score) {
            "0" -> {
                val newScorerState = scorer.copy(score = "15")
                if (team == ServingTeam.TEAM_A) currentState.copy(teamA = newScorerState) else currentState.copy(teamB = newScorerState)
            }
            "15" -> {
                val newScorerState = scorer.copy(score = "30")
                if (team == ServingTeam.TEAM_A) currentState.copy(teamA = newScorerState) else currentState.copy(teamB = newScorerState)
            }
            "30" -> {
                val newScorerState = scorer.copy(score = "40")
                if (team == ServingTeam.TEAM_A) currentState.copy(teamA = newScorerState) else currentState.copy(teamB = newScorerState)
            }
            "40" -> {
                when (other.score) {
                    "0", "15", "30" -> winGame(team, currentState)
                    "40" -> {
                        if (currentState.scoringMode == ScoringMode.BEST_OF_THREE_GOLDEN_POINT) {
                            winGame(team, currentState)
                        } else {
                            val newScorerState = scorer.copy(score = "Ad")
                            if (team == ServingTeam.TEAM_A) currentState.copy(teamA = newScorerState) else currentState.copy(teamB = newScorerState)
                        }
                    }
                    "Ad" -> {
                        val newOtherState = other.copy(score = "40")
                        if (team == ServingTeam.TEAM_A) {
                            currentState.copy(teamA = scorer, teamB = newOtherState)
                        } else {
                            currentState.copy(teamA = newOtherState, teamB = scorer)
                        }
                    }
                    else -> currentState // Should not happen
                }
            }
            "Ad" -> winGame(team, currentState)
            else -> currentState // Should not happen
        }
    }

    private fun addTieBreakPoint(team: ServingTeam, currentState: MatchState): MatchState {
        val (scorer, other) = if (team == ServingTeam.TEAM_A) {
            currentState.teamA to currentState.teamB
        } else {
            currentState.teamB to currentState.teamA
        }

        val scorerPoints = scorer.score.toIntOrNull() ?: 0
        val otherPoints = other.score.toIntOrNull() ?: 0
        val newScorerPoints = scorerPoints + 1

        if (newScorerPoints >= 7 && newScorerPoints - otherPoints >= 2) {
            return winSet(team, currentState)
        }

        val newScorerState = scorer.copy(score = newScorerPoints.toString())
        var newServer = currentState.server
        if ((newScorerPoints + otherPoints) % 2 == 1) {
            newServer = if (currentState.server == ServingTeam.TEAM_A) ServingTeam.TEAM_B else ServingTeam.TEAM_A
        }

        return if (team == ServingTeam.TEAM_A) {
            currentState.copy(teamA = newScorerState, server = newServer)
        } else {
            currentState.copy(teamB = newScorerState, server = newServer)
        }
    }

    private fun winGame(winner: ServingTeam, currentState: MatchState): MatchState {
        val (winningTeam, losingTeam) = if (winner == ServingTeam.TEAM_A) {
            currentState.teamA to currentState.teamB
        } else {
            currentState.teamB to currentState.teamA
        }

        val newWinnerGames = winningTeam.games + 1
        val newLoserGames = losingTeam.games

        if (newWinnerGames >= 6 && newWinnerGames - newLoserGames >= 2) {
            return winSet(winner, currentState)
        }

        val newServer = if (currentState.server == ServingTeam.TEAM_A) ServingTeam.TEAM_B else ServingTeam.TEAM_A

        if (newWinnerGames == 6 && newLoserGames == 6) {
            val newWinningTeamState = winningTeam.copy(score = "0", games = newWinnerGames)
            val newLosingTeamState = losingTeam.copy(score = "0")
            return if (winner == ServingTeam.TEAM_A) {
                currentState.copy(teamA = newWinningTeamState, teamB = newLosingTeamState, isTieBreakActive = true, server = newServer)
            } else {
                currentState.copy(teamA = newLosingTeamState, teamB = newWinningTeamState, isTieBreakActive = true, server = newServer)
            }
        }

        val newWinningTeamState = winningTeam.copy(score = "0", games = newWinnerGames)
        val newLosingTeamState = losingTeam.copy(score = "0")

        return if (winner == ServingTeam.TEAM_A) {
            currentState.copy(teamA = newWinningTeamState, teamB = newLosingTeamState, server = newServer)
        } else {
            currentState.copy(teamA = newLosingTeamState, teamB = newWinningTeamState, server = newServer)
        }
    }

    private fun winSet(winner: ServingTeam, currentState: MatchState): MatchState {
        val (winningTeam, losingTeam) = if (winner == ServingTeam.TEAM_A) {
            currentState.teamA to currentState.teamB
        } else {
            currentState.teamB to currentState.teamA
        }

        val newWinningTeamState = winningTeam.copy(
            score = "0",
            games = 0,
            sets = winningTeam.sets + 1
        )
        val newLosingTeamState = losingTeam.copy(score = "0", games = 0)

        val tempState = if (winner == ServingTeam.TEAM_A) {
            currentState.copy(teamA = newWinningTeamState, teamB = newLosingTeamState, isTieBreakActive = false)
        } else {
            currentState.copy(teamA = newLosingTeamState, teamB = newWinningTeamState, isTieBreakActive = false)
        }

        if (newWinningTeamState.sets == 2) {
            return tempState.copy(isMatchOver = true, winner = winner)
        }

        return tempState
    }

    fun undoLastPoint() {
        if (history.isNotEmpty()) {
            _matchState.value = history.removeAt(history.lastIndex)
        }
    }

    fun startNewMatch(teamAName: String, teamBName: String, scoringMode: ScoringMode) {
        val initialState = MatchState(
            teamA = com.padelscoring.model.TeamState(name = teamAName),
            teamB = com.padelscoring.model.TeamState(name = teamBName),
            scoringMode = scoringMode
        )
        _matchState.value = initialState
        history.clear()
    }

    fun resetMatch() {
        _matchState.value = MatchState()
        history.clear()
    }

    @VisibleForTesting
    fun setMatchStateForTest(state: MatchState) {
        _matchState.value = state
    }
}
