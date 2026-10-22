package com.padelscoring.viewmodel

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.padelscoring.data.local.PadelDatabase
import com.padelscoring.data.local.entity.UserEntity
import com.padelscoring.data.repository.MatchRepository
import com.padelscoring.data.repository.MatchRepositoryImpl
import com.padelscoring.data.repository.UserRepository
import com.padelscoring.data.repository.UserRepositoryImpl
import com.padelscoring.domain.model.Match
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
class MatchConfirmationViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: PadelDatabase
    private lateinit var matchRepository: MatchRepository
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = PadelDatabase.buildInMemoryDatabase(context)
        matchRepository = MatchRepositoryImpl(database.matchDao(), database.userDao())
        userRepository = UserRepositoryImpl(database.userDao(), database.matchDao())
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun testSlideToConfirmUpdatesStatusAndCalculatesRatings() = runTest(testDispatcher) {
        // Pre-populate 4 doubles players with baseline rating 3.00
        val u1 = UserEntity(id = "u1", name = "Player 1", level = 3.00, levelConfidence = 0.20, matchesPlayed = 0, matchesWon = 0, isGuest = false, preferredSide = "REVES")
        val u2 = UserEntity(id = "u2", name = "Player 2", level = 3.00, levelConfidence = 0.20, matchesPlayed = 0, matchesWon = 0, isGuest = false, preferredSide = "DRIVE")
        val u3 = UserEntity(id = "u3", name = "Player 3", level = 3.00, levelConfidence = 0.20, matchesPlayed = 0, matchesWon = 0, isGuest = false, preferredSide = "REVES")
        val u4 = UserEntity(id = "u4", name = "Player 4", level = 3.00, levelConfidence = 0.20, matchesPlayed = 0, matchesWon = 0, isGuest = false, preferredSide = "DRIVE")
        database.userDao().insertUsers(listOf(u1, u2, u3, u4))

        val match = Match(
            id = "match-101",
            matchType = "COMPETITIVE",
            team1Player1Id = "u1",
            team1Player2Id = "u2",
            team2Player1Id = "u3",
            team2Player2Id = "u4",
            set1T1 = 6,
            set1T2 = 2,
            set2T1 = 6,
            set2T2 = 3,
            winningTeam = 1,
            submittedByUserId = "u1",
            confirmationStatus = "PENDING",
            isRanked = true
        )
        matchRepository.submitMatch(match)

        val viewModel = MatchConfirmationViewModel(
            matchRepository = matchRepository,
            userRepository = userRepository,
            initialMatchId = "match-101",
            currentUserId = "u3"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("PENDING", viewModel.uiState.value.status)
        assertFalse(viewModel.uiState.value.isConfirmed)

        // Slide to confirm action
        viewModel.onSlideToConfirm()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("CONFIRMED", viewModel.uiState.value.status)
        assertTrue(viewModel.uiState.value.isConfirmed)
        assertFalse(viewModel.uiState.value.isDisputed)

        // Verify PDR rating updates
        val updatedU1 = database.userDao().getUserById("u1")!!
        val updatedU2 = database.userDao().getUserById("u2")!!
        val updatedU3 = database.userDao().getUserById("u3")!!
        val updatedU4 = database.userDao().getUserById("u4")!!

        // Winners ratings must increase
        assertTrue("Winner u1 level should increase", updatedU1.level > 3.00)
        assertTrue("Winner u2 level should increase", updatedU2.level > 3.00)
        assertEquals(1, updatedU1.matchesPlayed)
        assertEquals(1, updatedU1.matchesWon)
        assertEquals(1, updatedU1.currentStreak)

        // Losers ratings must decrease
        assertTrue("Loser u3 level should decrease", updatedU3.level < 3.00)
        assertTrue("Loser u4 level should decrease", updatedU4.level < 3.00)
        assertEquals(1, updatedU3.matchesPlayed)
        assertEquals(0, updatedU3.matchesWon)
        assertEquals(0, updatedU3.currentStreak)
    }

    @Test
    fun testMatchDisputeTransitionsStatusWithoutModifyingRatings() = runTest(testDispatcher) {
        val u1 = UserEntity(id = "userA", name = "Player A", level = 3.50, levelConfidence = 0.30, matchesPlayed = 5, matchesWon = 3, isGuest = false, preferredSide = "BOTH")
        val u2 = UserEntity(id = "userB", name = "Player B", level = 3.50, levelConfidence = 0.30, matchesPlayed = 5, matchesWon = 3, isGuest = false, preferredSide = "BOTH")
        val u3 = UserEntity(id = "userC", name = "Player C", level = 3.50, levelConfidence = 0.30, matchesPlayed = 5, matchesWon = 3, isGuest = false, preferredSide = "BOTH")
        val u4 = UserEntity(id = "userD", name = "Player D", level = 3.50, levelConfidence = 0.30, matchesPlayed = 5, matchesWon = 3, isGuest = false, preferredSide = "BOTH")
        database.userDao().insertUsers(listOf(u1, u2, u3, u4))

        val match = Match(
            id = "match-dispute-test",
            team1Player1Id = "userA",
            team1Player2Id = "userB",
            team2Player1Id = "userC",
            team2Player2Id = "userD",
            set1T1 = 6,
            set1T2 = 4,
            set2T1 = 6,
            set2T2 = 4,
            winningTeam = 1,
            submittedByUserId = "userA",
            confirmationStatus = "PENDING",
            isRanked = true
        )
        matchRepository.submitMatch(match)

        val viewModel = MatchConfirmationViewModel(
            matchRepository = matchRepository,
            userRepository = userRepository,
            initialMatchId = "match-dispute-test",
            currentUserId = "userC"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Disputing match
        val disputeReason = "Incorrect score recorded"
        viewModel.onDispute(disputeReason)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("DISPUTED", viewModel.uiState.value.status)
        assertTrue(viewModel.uiState.value.isDisputed)
        assertFalse(viewModel.uiState.value.isConfirmed)
        assertEquals(disputeReason, viewModel.uiState.value.disputeReason)

        // Verify players ratings remain strictly unmodified
        val retrievedU1 = database.userDao().getUserById("userA")!!
        val retrievedU3 = database.userDao().getUserById("userC")!!

        assertEquals(3.50, retrievedU1.level, 0.001)
        assertEquals(5, retrievedU1.matchesPlayed)
        assertEquals(3.50, retrievedU3.level, 0.001)
        assertEquals(5, retrievedU3.matchesPlayed)
    }
}
