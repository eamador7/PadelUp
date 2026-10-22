package com.padelscoring.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.padelscoring.data.local.PadelDatabase
import com.padelscoring.data.local.entity.MatchEntity
import com.padelscoring.data.local.entity.UserEntity
import com.padelscoring.data.repository.UserRepository
import com.padelscoring.data.repository.UserRepositoryImpl
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
class UserRepositoryTest {

    private lateinit var database: PadelDatabase
    private lateinit var repository: UserRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = PadelDatabase.buildInMemoryDatabase(context)
        repository = UserRepositoryImpl(database.userDao(), database.matchDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testUserProfileStatsAndStreaks() = runTest {
        val user = UserEntity(
            id = "user-profile-1",
            name = "Fernando Belasteguín",
            level = 6.85,
            levelConfidence = 0.95,
            matchesPlayed = 120,
            matchesWon = 90, // 90 / 120 = 75%
            preferredSide = "REVES",
            currentStreak = 4,
            bestStreak = 18,
            isGuest = false,
            createdAt = 1000L
        )
        database.userDao().insertUser(user)

        val profile = repository.observeUserProfile("user-profile-1").first()
        assertNotNull(profile)
        assertEquals("Fernando Belasteguín", profile?.playerName)
        assertEquals(6.85, profile?.level ?: 0.0, 0.001)
        assertEquals(75, profile?.winRate)
        assertEquals(120, profile?.matchesPlayed)
        assertEquals(4, profile?.currentStreak)
        assertEquals(18, profile?.bestStreak)
        assertEquals("REVÉS", profile?.preferredSide)
        assertEquals(0.95f, profile?.confidenceProgress ?: 0f, 0.001f)
    }

    @Test
    fun testTenMatchRollingFormCalculation() = runTest {
        val userId = "streak-user"
        val user = UserEntity(
            id = userId,
            name = "Agustín Tapia",
            level = 6.20,
            levelConfidence = 0.80,
            matchesPlayed = 12,
            matchesWon = 8,
            preferredSide = "DRIVE",
            isGuest = false
        )
        database.userDao().insertUser(user)

        // Insert 12 matches sorted chronologically: 10 wins and 2 losses
        // Oldest matches: match 1 (loss), match 2 (loss)
        // Recent matches (3 to 12): alternating wins and losses
        for (i in 1..12) {
            val won = i % 2 == 0 // even matches won, odd matches lost
            val match = MatchEntity(
                id = "m-$i",
                matchType = "COMPETITIVE",
                team1Player1Id = userId,
                team1Player2Id = "dummy1",
                team2Player1Id = "dummy2",
                team2Player2Id = "dummy3",
                set1T1 = if (won) 6 else 3,
                set1T2 = if (won) 3 else 6,
                set2T1 = if (won) 6 else 4,
                set2T2 = if (won) 4 else 6,
                winningTeam = if (won) 1 else 2,
                submittedByUserId = userId,
                confirmationStatus = "CONFIRMED",
                playedAt = i * 1000L // Chronological timestamp
            )
            database.matchDao().insertMatch(match)
        }

        val profile = repository.observeUserProfile(userId).first()
        assertNotNull(profile)
        val recentForm = profile?.recentForm ?: emptyList()

        // Should take exactly the 10 most recent matches (matches 12 down to 3)
        assertEquals(10, recentForm.size)

        // Match 12 was played at 12000L (won = true)
        assertEquals(true, recentForm[0])
        // Match 11 was played at 11000L (won = false)
        assertEquals(false, recentForm[1])
        // Match 10 was played at 10000L (won = true)
        assertEquals(true, recentForm[2])
    }
}
