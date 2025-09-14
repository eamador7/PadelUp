package com.padelscoring.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.padelscoring.model.MatchState
import com.padelscoring.model.ServingTeam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class GameViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: GameViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = GameViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addPoint progresses score from 0 to 40`() {
        val team = ServingTeam.TEAM_A
        assertEquals("0", viewModel.matchState.value.teamA.score)

        viewModel.addPoint(team)
        assertEquals("15", viewModel.matchState.value.teamA.score)

        viewModel.addPoint(team)
        assertEquals("30", viewModel.matchState.value.teamA.score)

        viewModel.addPoint(team)
        assertEquals("40", viewModel.matchState.value.teamA.score)
    }

    @Test
    fun `addPoint wins a simple game`() {
        val team = ServingTeam.TEAM_A
        repeat(3) { viewModel.addPoint(team) } // Score is 40-0

        assertEquals("40", viewModel.matchState.value.teamA.score)
        assertEquals(0, viewModel.matchState.value.teamA.games)

        viewModel.addPoint(team) // Win the game

        assertEquals(1, viewModel.matchState.value.teamA.games)
        assertEquals("0", viewModel.matchState.value.teamA.score)
        assertEquals("0", viewModel.matchState.value.teamB.score)
    }

    @Test
    fun `addPoint handles traditional deuce and advantage`() {
        val teamA = ServingTeam.TEAM_A
        val teamB = ServingTeam.TEAM_B

        // Go to 40-40
        repeat(3) { viewModel.addPoint(teamA) }
        repeat(3) { viewModel.addPoint(teamB) }
        assertEquals("40", viewModel.matchState.value.teamA.score)
        assertEquals("40", viewModel.matchState.value.teamB.score)

        // Go to Ad-40
        viewModel.addPoint(teamA)
        assertEquals("Ad", viewModel.matchState.value.teamA.score)
        assertEquals("40", viewModel.matchState.value.teamB.score)
    }

    @Test
    fun `addPoint wins game from advantage`() {
        val teamA = ServingTeam.TEAM_A
        val teamB = ServingTeam.TEAM_B

        // Go to Ad-40
        repeat(3) { viewModel.addPoint(teamA) }
        repeat(3) { viewModel.addPoint(teamB) }
        viewModel.addPoint(teamA)
        assertEquals("Ad", viewModel.matchState.value.teamA.score)

        // Win the game
        viewModel.addPoint(teamA)
        assertEquals(1, viewModel.matchState.value.teamA.games)
        assertEquals("0", viewModel.matchState.value.teamA.score)
    }

    @Test
    fun `addPoint returns to deuce from advantage`() {
        val teamA = ServingTeam.TEAM_A
        val teamB = ServingTeam.TEAM_B

        // Go to Ad-40
        repeat(3) { viewModel.addPoint(teamA) }
        repeat(3) { viewModel.addPoint(teamB) }
        viewModel.addPoint(teamA)
        assertEquals("Ad", viewModel.matchState.value.teamA.score)

        // Go back to 40-40
        viewModel.addPoint(teamB)
        assertEquals("40", viewModel.matchState.value.teamA.score)
        assertEquals("40", viewModel.matchState.value.teamB.score)
    }

    @Test
    fun `resetMatch resets the state`() {
        val teamA = ServingTeam.TEAM_A
        viewModel.addPoint(teamA)
        viewModel.addPoint(teamA)

        viewModel.resetMatch()

        val initialState = MatchState()
        assertEquals(initialState, viewModel.matchState.value)
    }

    @Test
    fun `addPoint wins game with golden point`() {
        // Set up the match with Golden Point scoring
        val goldenPointState = MatchState(scoringMode = com.padelscoring.model.ScoringMode.BEST_OF_THREE_GOLDEN_POINT)
        viewModel.setMatchStateForTest(goldenPointState)

        val teamA = ServingTeam.TEAM_A
        val teamB = ServingTeam.TEAM_B

        // Go to 40-40
        repeat(3) { viewModel.addPoint(teamA) }
        repeat(3) { viewModel.addPoint(teamB) }
        assertEquals("40", viewModel.matchState.value.teamA.score)
        assertEquals("40", viewModel.matchState.value.teamB.score)
        assertEquals(0, viewModel.matchState.value.teamA.games)

        // Win the game with the golden point
        viewModel.addPoint(teamA)
        assertEquals(1, viewModel.matchState.value.teamA.games)
        assertEquals("0", viewModel.matchState.value.teamA.score)
        assertEquals("0", viewModel.matchState.value.teamB.score)
    }

    @Test
    fun `winGame triggers set win at 5-3 to 6-3`() {
        val teamA = ServingTeam.TEAM_A
        val initialState = MatchState(
            teamA = com.padelscoring.model.TeamState(games = 5),
            teamB = com.padelscoring.model.TeamState(games = 3)
        )
        viewModel.setMatchStateForTest(initialState)

        // Win the 4 points to win the game
        repeat(4) { viewModel.addPoint(teamA) }

        val finalState = viewModel.matchState.value
        assertEquals(1, finalState.teamA.sets)
        assertEquals(0, finalState.teamA.games)
        assertEquals(0, finalState.teamB.games)
    }

    @Test
    fun `winGame does not win set at 6-5`() {
        val teamA = ServingTeam.TEAM_A
        val initialState = MatchState(
            teamA = com.padelscoring.model.TeamState(games = 5),
            teamB = com.padelscoring.model.TeamState(games = 5)
        )
        viewModel.setMatchStateForTest(initialState)

        // Win a game to make it 6-5
        repeat(4) { viewModel.addPoint(teamA) }

        val finalState = viewModel.matchState.value
        assertEquals(0, finalState.teamA.sets)
        assertEquals(6, finalState.teamA.games)
        assertEquals(5, finalState.teamB.games)
    }

    @Test
    fun `winGame wins set at 7-5`() {
        val teamA = ServingTeam.TEAM_A
        val initialState = MatchState(
            teamA = com.padelscoring.model.TeamState(games = 6),
            teamB = com.padelscoring.model.TeamState(games = 5)
        )
        viewModel.setMatchStateForTest(initialState)

        // Win a game to make it 7-5
        repeat(4) { viewModel.addPoint(teamA) }

        val finalState = viewModel.matchState.value
        assertEquals(1, finalState.teamA.sets)
        assertEquals(0, finalState.teamA.games)
        assertEquals(0, finalState.teamB.games)
    }

    @Test
    fun `winGame triggers tie-break at 6-6`() {
        val teamA = ServingTeam.TEAM_A
        val initialState = MatchState(
            teamA = com.padelscoring.model.TeamState(games = 5),
            teamB = com.padelscoring.model.TeamState(games = 6)
        )
        viewModel.setMatchStateForTest(initialState)

        // Win a game to make it 6-6
        repeat(4) { viewModel.addPoint(teamA) }

        val finalState = viewModel.matchState.value
        assertEquals(6, finalState.teamA.games)
        assertEquals(6, finalState.teamB.games)
        assertEquals(true, finalState.isTieBreakActive)
    }

    @Test
    fun `addPoint delegates to addTieBreakPoint when tie-break is active`() {
        val teamA = ServingTeam.TEAM_A
        val initialState = MatchState(isTieBreakActive = true)
        viewModel.setMatchStateForTest(initialState)

        viewModel.addPoint(teamA)
        assertEquals("1", viewModel.matchState.value.teamA.score)
    }

    @Test
    fun `tie-break point progression`() {
        val teamA = ServingTeam.TEAM_A
        val teamB = ServingTeam.TEAM_B
        val initialState = MatchState(isTieBreakActive = true)
        viewModel.setMatchStateForTest(initialState)

        viewModel.addPoint(teamA) // 1-0
        viewModel.addPoint(teamB) // 1-1
        viewModel.addPoint(teamA) // 2-1

        assertEquals("2", viewModel.matchState.value.teamA.score)
        assertEquals("1", viewModel.matchState.value.teamB.score)
    }

    @Test
    fun `tie-break win at 7-5`() {
        val teamA = ServingTeam.TEAM_A
        val initialState = MatchState(
            isTieBreakActive = true,
            teamA = com.padelscoring.model.TeamState(score = "6"),
            teamB = com.padelscoring.model.TeamState(score = "5")
        )
        viewModel.setMatchStateForTest(initialState)

        viewModel.addPoint(teamA) // 7-5, wins tie-break and set

        val finalState = viewModel.matchState.value
        assertEquals(1, finalState.teamA.sets)
        assertEquals(0, finalState.teamA.games)
        assertEquals(0, finalState.teamB.games)
        assertEquals(false, finalState.isTieBreakActive)
    }

    @Test
    fun `tie-break continues at 7-6`() {
        val teamA = ServingTeam.TEAM_A
        val initialState = MatchState(
            isTieBreakActive = true,
            teamA = com.padelscoring.model.TeamState(score = "6"),
            teamB = com.padelscoring.model.TeamState(score = "6")
        )
        viewModel.setMatchStateForTest(initialState)

        viewModel.addPoint(teamA) // 7-6

        val finalState = viewModel.matchState.value
        assertEquals(0, finalState.teamA.sets) // No set win yet
        assertEquals("7", finalState.teamA.score)
        assertEquals("6", finalState.teamB.score)
    }

    @Test
    fun `tie-break win at 9-7`() {
        val teamA = ServingTeam.TEAM_A
        val initialState = MatchState(
            isTieBreakActive = true,
            teamA = com.padelscoring.model.TeamState(score = "8"),
            teamB = com.padelscoring.model.TeamState(score = "7")
        )
        viewModel.setMatchStateForTest(initialState)

        viewModel.addPoint(teamA) // 9-7, wins tie-break and set

        val finalState = viewModel.matchState.value
        assertEquals(1, finalState.teamA.sets)
        assertEquals(0, finalState.teamA.games)
        assertEquals(false, finalState.isTieBreakActive)
    }

    @Test
    fun `undo reverts the last point`() {
        val teamA = ServingTeam.TEAM_A
        val initialState = viewModel.matchState.value

        viewModel.addPoint(teamA) // Score a point
        assert(initialState != viewModel.matchState.value)

        viewModel.undoLastPoint() // Undo the point
        assertEquals(initialState, viewModel.matchState.value)
    }

    @Test
    fun `undo works with multiple steps`() {
        val teamA = ServingTeam.TEAM_A
        viewModel.addPoint(teamA) // 15-0
        val stateAfterFirstPoint = viewModel.matchState.value
        viewModel.addPoint(teamA) // 30-0
        assert(stateAfterFirstPoint != viewModel.matchState.value)

        viewModel.undoLastPoint()
        assertEquals(stateAfterFirstPoint, viewModel.matchState.value)
    }

    @Test
    fun `undo does nothing when history is empty`() {
        val initialState = viewModel.matchState.value
        viewModel.undoLastPoint()
        assertEquals(initialState, viewModel.matchState.value)
    }

    @Test
    fun `undo does not work after reset`() {
        val teamA = ServingTeam.TEAM_A
        viewModel.addPoint(teamA)
        assert(viewModel.matchState.value != MatchState())

        viewModel.resetMatch()
        val resetState = viewModel.matchState.value
        assertEquals(MatchState(), resetState)

        viewModel.undoLastPoint() // Should do nothing
        assertEquals(resetState, viewModel.matchState.value)
    }

    @Test
    fun `undo reverts a point scored in a tie-break`() {
        val teamA = ServingTeam.TEAM_A
        val initialState = MatchState(
            isTieBreakActive = true,
            teamA = com.padelscoring.model.TeamState(score = "2"),
            teamB = com.padelscoring.model.TeamState(score = "3")
        )
        viewModel.setMatchStateForTest(initialState)

        viewModel.addPoint(teamA) // Score is now 3-3
        assertEquals("3", viewModel.matchState.value.teamA.score)

        viewModel.undoLastPoint() // Undo the point
        assertEquals(initialState, viewModel.matchState.value)
    }

    @Test
    fun `startNewMatch correctly initializes a new match`() {
        val teamAName = "Team Alpha"
        val teamBName = "The B-Team"
        val scoringMode = com.padelscoring.model.ScoringMode.BEST_OF_THREE_GOLDEN_POINT

        viewModel.startNewMatch(teamAName, teamBName, scoringMode)

        val state = viewModel.matchState.value
        val expectedState = MatchState(
            teamA = com.padelscoring.model.TeamState(name = teamAName),
            teamB = com.padelscoring.model.TeamState(name = teamBName),
            scoringMode = scoringMode
        )

        assertEquals(expectedState, state)
    }

    @Test
    fun `server rotates after a regular game`() {
        val teamA = ServingTeam.TEAM_A
        assertEquals(ServingTeam.TEAM_A, viewModel.matchState.value.server)

        // Win a game
        repeat(4) { viewModel.addPoint(teamA) }

        assertEquals(ServingTeam.TEAM_B, viewModel.matchState.value.server)
    }

    @Test
    fun `server rotates correctly in a tie-break`() {
        val teamA = ServingTeam.TEAM_A
        val teamB = ServingTeam.TEAM_B
        val initialState = MatchState(isTieBreakActive = true, server = ServingTeam.TEAM_A)
        viewModel.setMatchStateForTest(initialState)

        // Point 1 (total 1, odd) -> server should switch to B
        viewModel.addPoint(teamA)
        assertEquals(ServingTeam.TEAM_B, viewModel.matchState.value.server)

        // Point 2 (total 2, even) -> server should not switch
        viewModel.addPoint(teamA)
        assertEquals(ServingTeam.TEAM_B, viewModel.matchState.value.server)

        // Point 3 (total 3, odd) -> server should switch to A
        viewModel.addPoint(teamB)
        assertEquals(ServingTeam.TEAM_A, viewModel.matchState.value.server)
    }

    @Test
    fun `match is won after winning 2 sets`() {
        val teamA = ServingTeam.TEAM_A
        val initialState = MatchState(
            teamA = com.padelscoring.model.TeamState(sets = 1, games = 5),
            teamB = com.padelscoring.model.TeamState(sets = 0, games = 0)
        )
        viewModel.setMatchStateForTest(initialState)

        // Win the last game of the second set
        repeat(4) { viewModel.addPoint(teamA) }

        val finalState = viewModel.matchState.value
        assertEquals(true, finalState.isMatchOver)
        assertEquals(ServingTeam.TEAM_A, finalState.winner)
    }
}
