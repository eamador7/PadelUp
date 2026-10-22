package com.padelscoring.viewmodel

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.padelscoring.data.local.PadelDatabase
import com.padelscoring.data.repository.TournamentRepository
import com.padelscoring.data.repository.TournamentRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@OptIn(ExperimentalCoroutinesApi::class)
class AmericanoTournamentViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: PadelDatabase
    private lateinit var tournamentRepository: TournamentRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = PadelDatabase.buildInMemoryDatabase(context)
        tournamentRepository = TournamentRepositoryImpl(database.tournamentDao())
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun testLiveLeaderboardRecalculationOnScoreChanged() = runTest(testDispatcher) {
        val playerNames = listOf("Ale Galán", "Arturo Coello", "Juan Lebrón", "Agustín Tapia")
        val tournamentId = tournamentRepository.createAmericanoTournament(
            name = "Test Americano",
            playerNames = playerNames,
            targetPoints = 32,
            totalCourts = 1,
            isOffline = true
        )

        val viewModel = AmericanoTournamentViewModel(tournamentRepository, tournamentId)
        testDispatcher.scheduler.advanceUntilIdle()

        val stateBefore = viewModel.uiState.value
        assertEquals(1, stateBefore.currentRoundNumber)
        assertEquals(1, stateBefore.courtMatches.size)
        val activeMatch = stateBefore.courtMatches[0]
        assertFalse(activeMatch.isCompleted)

        // Submitting score 18 - 14
        viewModel.onScoreChanged(activeMatch.matchId, 18, 14)
        testDispatcher.scheduler.advanceUntilIdle()

        val stateAfter = viewModel.uiState.value
        assertEquals(18, stateAfter.courtMatches[0].teamAScore)
        assertEquals(14, stateAfter.courtMatches[0].teamBScore)
        assertTrue(stateAfter.courtMatches[0].isCompleted)

        // Leaderboard standings verification
        val standings = stateAfter.standings
        assertEquals(4, standings.size)
        // Team 1 players should have 18 points each
        val topScorer1 = standings[0]
        val topScorer2 = standings[1]
        assertEquals(18, topScorer1.totalPoints)
        assertEquals(18, topScorer2.totalPoints)
        assertEquals(4, topScorer1.pointDiff) // 18 - 14 = +4
        assertEquals(1, topScorer1.won)

        // Team 2 players should have 14 points each
        val bottomScorer1 = standings[2]
        val bottomScorer2 = standings[3]
        assertEquals(14, bottomScorer1.totalPoints)
        assertEquals(14, bottomScorer2.totalPoints)
        assertEquals(-4, bottomScorer1.pointDiff) // 14 - 18 = -4
        assertEquals(0, bottomScorer1.won)
    }

    @Test
    fun testNextRoundGenerationGate() = runTest(testDispatcher) {
        val playerNames = listOf("P1", "P2", "P3", "P4", "P5", "P6", "P7", "P8")
        val tournamentId = tournamentRepository.createAmericanoTournament(
            name = "Dual Court Americano",
            playerNames = playerNames,
            targetPoints = 32,
            totalCourts = 2,
            isOffline = true
        )

        val viewModel = AmericanoTournamentViewModel(tournamentRepository, tournamentId)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.courtMatches.size)
        // Incomplete matches -> canGenerateNextRound must be false
        assertFalse(viewModel.uiState.value.canGenerateNextRound)

        // Complete only Court 1
        val court1 = state.courtMatches.first { it.courtNumber == 1 }
        val court2 = state.courtMatches.first { it.courtNumber == 2 }

        viewModel.onScoreChanged(court1.matchId, 20, 12)
        testDispatcher.scheduler.advanceUntilIdle()

        // Court 2 is still incomplete -> canGenerateNextRound must remain false
        assertFalse(viewModel.uiState.value.canGenerateNextRound)

        // Complete Court 2
        viewModel.onScoreChanged(court2.matchId, 16, 16)
        testDispatcher.scheduler.advanceUntilIdle()

        // Both courts completed -> canGenerateNextRound evaluates to true
        assertTrue(viewModel.uiState.value.canGenerateNextRound)
    }

    @Test
    fun testRoundAdvancementWorkflow() = runTest(testDispatcher) {
        val playerNames = listOf("P1", "P2", "P3", "P4")
        val tournamentId = tournamentRepository.createAmericanoTournament(
            name = "Progression Americano",
            playerNames = playerNames,
            targetPoints = 32,
            totalCourts = 1,
            isOffline = true
        )

        val viewModel = AmericanoTournamentViewModel(tournamentRepository, tournamentId)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.currentRoundNumber)
        val matchR1 = viewModel.uiState.value.courtMatches[0]

        // Complete Round 1 match
        viewModel.onScoreChanged(matchR1.matchId, 19, 13)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canGenerateNextRound)

        // Advance to Next Round
        viewModel.onGenerateNextRound()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify Round 2 is now active
        val stateR2 = viewModel.uiState.value
        assertEquals(2, stateR2.currentRoundNumber)
        assertEquals(1, stateR2.courtMatches.size)
        // New round match should be incomplete
        assertFalse(stateR2.courtMatches[0].isCompleted)
        // canGenerateNextRound is reset to false
        assertFalse(stateR2.canGenerateNextRound)
    }
}
