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
import org.junit.Assert.assertNotNull
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
class OfflineLauncherViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: PadelDatabase
    private lateinit var repository: TournamentRepository
    private lateinit var viewModel: OfflineLauncherViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = PadelDatabase.buildInMemoryDatabase(context)
        repository = TournamentRepositoryImpl(database.tournamentDao())
        viewModel = OfflineLauncherViewModel(repository)
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun testSetPlayerCountAndAdjustRoster() {
        assertEquals(8, viewModel.uiState.value.selectedPlayerCount)

        viewModel.setPlayerCount(12)
        assertEquals(12, viewModel.uiState.value.selectedPlayerCount)
        assertTrue(viewModel.uiState.value.playerRoster.size >= 12)

        viewModel.setTargetPoints(24)
        assertEquals(24, viewModel.uiState.value.selectedTargetPoints)
    }

    @Test
    fun testRosterGuestPlayerEditing() {
        viewModel.addGuestPlayer("Special Guest")
        assertTrue(viewModel.uiState.value.playerRoster.contains("Special Guest"))

        val index = viewModel.uiState.value.playerRoster.indexOf("Special Guest")
        viewModel.updateGuestName(index, "VIP Guest")
        assertEquals("VIP Guest", viewModel.uiState.value.playerRoster[index])

        viewModel.removeGuestPlayer(index)
        assertTrue(!viewModel.uiState.value.playerRoster.contains("VIP Guest"))
    }

    @Test
    fun testKickoffTournamentCreatesAmericano() = runTest(testDispatcher) {
        var launchedId: String? = null

        viewModel.setPlayerCount(4)
        viewModel.setTargetPoints(32)
        viewModel.setTournamentName("Friday Fast Americano")

        viewModel.kickoffTournament { id ->
            launchedId = id
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(launchedId)
        val created = database.tournamentDao().getTournamentById(launchedId!!)
        assertNotNull(created)
        assertEquals("Friday Fast Americano", created?.name)
        assertEquals(32, created?.targetPoints)
        assertEquals(1, created?.totalCourts)
    }
}
