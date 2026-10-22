package com.padelscoring.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.padelscoring.data.local.PadelDatabase
import com.padelscoring.data.repository.TournamentRepository
import com.padelscoring.data.repository.TournamentRepositoryImpl
import com.padelscoring.domain.model.TournamentFormat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TournamentRepositoryTest {

    private lateinit var database: PadelDatabase
    private lateinit var repository: TournamentRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = PadelDatabase.buildInMemoryDatabase(context)
        repository = TournamentRepositoryImpl(database.tournamentDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testAmericanoTournamentCreationAndSchedule() = runTest {
        val players = listOf("P1", "P2", "P3", "P4")
        val tournamentId = repository.createAmericanoTournament(
            name = "Test Americano 4P",
            playerNames = players,
            targetPoints = 32,
            totalCourts = 1,
            isOffline = true
        )

        assertNotNull(tournamentId)

        val state = repository.observeTournament(tournamentId).first { it.tournament != null }
        assertEquals("Test Americano 4P", state.tournament?.name)
        assertEquals(32, state.targetPoints)
        assertEquals(1, state.currentRoundNumber)
        assertEquals(3, state.totalRounds)
        assertEquals(4, state.standings.size)
        assertEquals(1, state.courtMatches.size)
    }

    @Test
    fun testScoreConservationEnforcement() = runTest {
        val players = listOf("P1", "P2", "P3", "P4")
        val tournamentId = repository.createAmericanoTournament(
            name = "Conservation Cup",
            playerNames = players,
            targetPoints = 32,
            totalCourts = 1,
            isOffline = true
        )

        val state = repository.observeTournament(tournamentId).first { it.courtMatches.isNotEmpty() }
        val matchId = state.courtMatches[0].matchId

        // Invalid score sum: 18 + 10 = 28 != 32
        try {
            repository.recordMatchScore(matchId, 18, 10)
            fail("Expected IllegalArgumentException for violated score conservation")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("must equal target points") == true)
        }

        // Valid score sum: 18 + 14 = 32
        repository.recordMatchScore(matchId, 18, 14)

        val updated = repository.observeTournament(tournamentId).first { it.courtMatches[0].isCompleted }
        assertEquals(18, updated.courtMatches[0].teamAScore)
        assertEquals(14, updated.courtMatches[0].teamBScore)
        assertTrue(updated.courtMatches[0].isCompleted)
    }

    @Test
    fun testMexicanoDynamicTieredPairings() = runTest {
        val players = (1..8).map { "Player $it" }
        val tournamentId = repository.createMexicanoTournament(
            name = "Mexicano Tiered",
            playerNames = players,
            targetPoints = 32,
            totalCourts = 2,
            isOffline = true
        )

        val state = repository.observeTournament(tournamentId).first { it.courtMatches.size == 2 }
        assertEquals(1, state.currentRoundNumber)

        val court1 = state.courtMatches.first { it.courtNumber == 1 }
        val court2 = state.courtMatches.first { it.courtNumber == 2 }

        // Court 1: 22 - 10
        repository.recordMatchScore(court1.matchId, 22, 10)
        // Court 2: 18 - 14
        repository.recordMatchScore(court2.matchId, 18, 14)

        // Advance to Round 2
        repository.advanceToNextRound(tournamentId)

        val round2State = repository.observeTournament(tournamentId).first { it.currentRoundNumber == 2 }
        assertEquals(2, round2State.currentRoundNumber)
        assertEquals(2, round2State.courtMatches.size)

        // Court 1 in Mexicano Round 2 must consist of the top 4 ranked players
        val round2Court1 = round2State.courtMatches.first { it.courtNumber == 1 }
        val round2Court1Players = setOf(
            round2Court1.teamANames.first,
            round2Court1.teamANames.second,
            round2Court1.teamBNames.first,
            round2Court1.teamBNames.second
        )

        // The top 4 players by leaderboard points after round 1
        val top4Names = round2State.standings.take(4).map { it.name }.toSet()
        assertEquals(top4Names, round2Court1Players)
    }
}
