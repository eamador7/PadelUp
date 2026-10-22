package com.padelscoring.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.padelscoring.data.local.PadelDatabase
import com.padelscoring.data.local.entity.UserEntity
import com.padelscoring.data.repository.MatchRepository
import com.padelscoring.data.repository.MatchRepositoryImpl
import com.padelscoring.domain.model.Match
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MatchRepositoryTest {

    private lateinit var database: PadelDatabase
    private lateinit var repository: MatchRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = PadelDatabase.buildInMemoryDatabase(context)
        repository = MatchRepositoryImpl(database.matchDao(), database.userDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testSubmitMatchAndObservePending() = runTest {
        val match = Match(
            id = "pending-match-1",
            team1Player1Id = "p1",
            team1Player2Id = "p2",
            team2Player1Id = "p3",
            team2Player2Id = "p4",
            set1T1 = 6,
            set1T2 = 3,
            set2T1 = 6,
            set2T2 = 4,
            winningTeam = 1,
            submittedByUserId = "p1",
            confirmationStatus = "PENDING",
            isRanked = true
        )

        repository.submitMatch(match)

        val pending = repository.observePendingMatches().first()
        assertEquals(1, pending.size)
        assertEquals("pending-match-1", pending[0].id)
        assertEquals("PENDING", pending[0].confirmationStatus)

        val retrieved = repository.getMatch("pending-match-1")
        assertNotNull(retrieved)
        assertEquals(1, retrieved?.winningTeam)
    }

    @Test
    fun testConfirmMatchUpdatesPdrRatings() = runTest {
        val u1 = UserEntity(id = "user1", name = "Player 1", level = 2.50, levelConfidence = 0.20, matchesPlayed = 0, matchesWon = 0, isGuest = false, preferredSide = "REVES")
        val u2 = UserEntity(id = "user2", name = "Player 2", level = 2.50, levelConfidence = 0.20, matchesPlayed = 0, matchesWon = 0, isGuest = false, preferredSide = "DRIVE")
        val u3 = UserEntity(id = "user3", name = "Player 3", level = 2.50, levelConfidence = 0.20, matchesPlayed = 0, matchesWon = 0, isGuest = false, preferredSide = "REVES")
        val u4 = UserEntity(id = "user4", name = "Player 4", level = 2.50, levelConfidence = 0.20, matchesPlayed = 0, matchesWon = 0, isGuest = false, preferredSide = "DRIVE")
        database.userDao().insertUsers(listOf(u1, u2, u3, u4))

        val match = Match(
            id = "ranked-match-1",
            team1Player1Id = "user1",
            team1Player2Id = "user2",
            team2Player1Id = "user3",
            team2Player2Id = "user4",
            set1T1 = 6,
            set1T2 = 1,
            set2T1 = 6,
            set2T2 = 2,
            winningTeam = 1,
            submittedByUserId = "user1",
            confirmationStatus = "PENDING",
            isRanked = true
        )
        repository.submitMatch(match)

        repository.confirmMatch("ranked-match-1", "user3")

        val matchAfter = repository.getMatch("ranked-match-1")
        assertEquals("CONFIRMED", matchAfter?.confirmationStatus)

        // Winners ratings should increase
        val user1After = database.userDao().getUserById("user1")!!
        val user2After = database.userDao().getUserById("user2")!!
        assertTrue(user1After.level > 2.50)
        assertTrue(user2After.level > 2.50)
        assertEquals(1, user1After.matchesWon)

        // Losers ratings should decrease
        val user3After = database.userDao().getUserById("user3")!!
        val user4After = database.userDao().getUserById("user4")!!
        assertTrue(user3After.level < 2.50)
        assertTrue(user4After.level < 2.50)
        assertEquals(0, user3After.matchesWon)
    }

    @Test
    fun testDisputeMatchDoesNotUpdateRatings() = runTest {
        val u1 = UserEntity(id = "disputeUser1", name = "Player 1", level = 3.20, levelConfidence = 0.40, matchesPlayed = 10, matchesWon = 6, isGuest = false, preferredSide = "BOTH")
        val u2 = UserEntity(id = "disputeUser2", name = "Player 2", level = 3.20, levelConfidence = 0.40, matchesPlayed = 10, matchesWon = 6, isGuest = false, preferredSide = "BOTH")
        database.userDao().insertUsers(listOf(u1, u2))

        val match = Match(
            id = "disputed-match-id",
            team1Player1Id = "disputeUser1",
            team1Player2Id = null,
            team2Player1Id = "disputeUser2",
            team2Player2Id = null,
            set1T1 = 6,
            set1T2 = 4,
            set2T1 = 4,
            set2T2 = 6,
            winningTeam = 1,
            submittedByUserId = "disputeUser1",
            confirmationStatus = "PENDING",
            isRanked = false
        )
        repository.submitMatch(match)

        repository.disputeMatch("disputed-match-id", "disputeUser2", "Injury call disagreement")

        val matchAfter = repository.getMatch("disputed-match-id")
        assertEquals("DISPUTED", matchAfter?.confirmationStatus)

        val user1After = database.userDao().getUserById("disputeUser1")!!
        assertEquals(3.20, user1After.level, 0.001)
        assertEquals(10, user1After.matchesPlayed)
    }
}
