package com.padelscoring.viewmodel

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.padelscoring.data.local.PadelDatabase
import com.padelscoring.data.local.entity.UserEntity
import com.padelscoring.data.repository.UserRepository
import com.padelscoring.data.repository.UserRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@OptIn(ExperimentalCoroutinesApi::class)
class PlayerProfileViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: PadelDatabase
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = PadelDatabase.buildInMemoryDatabase(context)
        userRepository = UserRepositoryImpl(database.userDao(), database.matchDao())
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadUserProfileUpdatesUiState() = runTest(testDispatcher) {
        val user = UserEntity(
            id = "profile-user-1",
            name = "Arturo Coello",
            level = 6.90,
            levelConfidence = 0.98,
            matchesPlayed = 50,
            matchesWon = 40,
            preferredSide = "DRIVE",
            currentStreak = 7,
            bestStreak = 15,
            isGuest = false
        )
        database.userDao().insertUser(user)

        val viewModel = PlayerProfileViewModel(userRepository, "profile-user-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Arturo Coello", state.playerName)
        assertEquals(6.90, state.level, 0.001)
        assertEquals(80, state.winRate) // 40 / 50 = 80%
        assertEquals(50, state.matchesPlayed)
        assertEquals(7, state.currentStreak)
        assertEquals(15, state.bestStreak)
        assertEquals("DRIVE", state.preferredSide)
    }
}
