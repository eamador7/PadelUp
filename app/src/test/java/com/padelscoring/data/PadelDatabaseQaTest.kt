package com.padelscoring.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.padelscoring.data.local.PadelDatabase
import com.padelscoring.data.local.dao.GroupDao
import com.padelscoring.data.local.dao.MatchDao
import com.padelscoring.data.local.dao.TournamentDao
import com.padelscoring.data.local.dao.UserDao
import com.padelscoring.data.local.entity.GroupEntity
import com.padelscoring.data.local.entity.GroupMemberEntity
import com.padelscoring.data.local.entity.MatchConfirmationEntity
import com.padelscoring.data.local.entity.MatchEntity
import com.padelscoring.data.local.entity.TournamentEntity
import com.padelscoring.data.local.entity.TournamentMatchEntity
import com.padelscoring.data.local.entity.TournamentParticipantEntity
import com.padelscoring.data.local.entity.TournamentRoundEntity
import com.padelscoring.data.local.entity.UserEntity
import com.padelscoring.data.local.mapper.toDomain
import com.padelscoring.data.local.mapper.toEntity
import com.padelscoring.domain.model.CourtSide
import com.padelscoring.domain.model.Match
import com.padelscoring.domain.model.Tournament
import com.padelscoring.domain.model.TournamentFormat
import com.padelscoring.domain.model.TournamentMatch
import com.padelscoring.domain.model.TournamentParticipant
import com.padelscoring.domain.model.TournamentStatus
import com.padelscoring.domain.model.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric JVM integration tests for Room SQLite database, reactive Flow DAOs,
 * foreign keys with cascading deletions, and entity mappers.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PadelDatabaseQaTest {

    private lateinit var database: PadelDatabase
    private lateinit var userDao: UserDao
    private lateinit var matchDao: MatchDao
    private lateinit var tournamentDao: TournamentDao
    private lateinit var groupDao: GroupDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = PadelDatabase.buildInMemoryDatabase(context)
        userDao = database.userDao()
        matchDao = database.matchDao()
        tournamentDao = database.tournamentDao()
        groupDao = database.groupDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // --- 1. User Insertion & Flow Observation ---

    @Test
    fun testUserInsertionAndFlow() = runTest {
        val user = UserEntity(
            id = "user-1",
            name = "Fernando Belasteguín",
            email = "bela@padel.com",
            avatarUrl = "https://example.com/bela.png",
            level = 6.85,
            levelConfidence = 0.95,
            matchesPlayed = 120,
            matchesWon = 105,
            preferredSide = "REVES",
            currentStreak = 8,
            bestStreak = 24,
            isGuest = false,
            createdAt = 1000L
        )

        userDao.insertUser(user)

        val retrieved = userDao.getUserFlow("user-1").first()
        assertNotNull(retrieved)
        assertEquals("Fernando Belasteguín", retrieved?.name)
        assertEquals("bela@padel.com", retrieved?.email)
        assertEquals(6.85, retrieved?.level ?: 0.0, 0.001)
        assertEquals(0.95, retrieved?.levelConfidence ?: 0.0, 0.001)
        assertEquals(120, retrieved?.matchesPlayed)
        assertEquals(105, retrieved?.matchesWon)
        assertEquals("REVES", retrieved?.preferredSide)
        assertEquals(8, retrieved?.currentStreak)
        assertEquals(24, retrieved?.bestStreak)
        assertEquals(false, retrieved?.isGuest)
        assertEquals(1000L, retrieved?.createdAt)
    }

    // --- 2. Guest Users Query Filtering ---

    @Test
    fun testGuestUsersFiltering() = runTest {
        val regularUser = UserEntity(
            id = "u-reg-1",
            name = "Pro Player",
            isGuest = false,
            createdAt = 100L
        )
        val guest1 = UserEntity(
            id = "u-guest-1",
            name = "Guest Alpha",
            isGuest = true,
            createdAt = 200L
        )
        val guest2 = UserEntity(
            id = "u-guest-2",
            name = "Guest Beta",
            isGuest = true,
            createdAt = 500L
        )

        userDao.insertUsers(listOf(regularUser, guest1, guest2))

        val guests = userDao.getGuestUsers().first()
        assertEquals(2, guests.size)
        // Ordered by createdAt DESC
        assertEquals("u-guest-2", guests[0].id)
        assertEquals("u-guest-1", guests[1].id)
    }

    // --- 3. Atomic Rating and Stats Updates ---

    @Test
    fun testAtomicUserRatingUpdate() = runTest {
        val user = UserEntity(
            id = "user-rating",
            name = "Arturo Coello",
            level = 6.50,
            levelConfidence = 0.50,
            matchesPlayed = 10,
            matchesWon = 8
        )
        userDao.insertUser(user)

        // Increment with a win (+1 wonInc)
        userDao.updateRating(
            id = "user-rating",
            level = 6.62,
            confidence = 0.58,
            wonInc = 1
        )

        val updated = userDao.getUserById("user-rating")
        assertNotNull(updated)
        assertEquals(6.62, updated?.level ?: 0.0, 0.001)
        assertEquals(0.58, updated?.levelConfidence ?: 0.0, 0.001)
        assertEquals(11, updated?.matchesPlayed)
        assertEquals(9, updated?.matchesWon)

        // Increment with a loss (+0 wonInc)
        userDao.updateRating(
            id = "user-rating",
            level = 6.55,
            confidence = 0.62,
            wonInc = 0
        )

        val afterLoss = userDao.getUserById("user-rating")
        assertEquals(12, afterLoss?.matchesPlayed)
        assertEquals(9, afterLoss?.matchesWon)
    }

    // --- 4. Tournament Multi-Tier Cascading Deletion ---

    @Test
    fun testTournamentCascadingDeletion() = runTest {
        val tournamentId = "t-100"
        val tournament = TournamentEntity(
            id = tournamentId,
            name = "Friday Night Americano",
            format = "AMERICANO",
            targetPoints = 32,
            totalCourts = 2,
            status = "IN_PROGRESS",
            createdById = "creator-1"
        )
        tournamentDao.insertTournament(tournament)

        val participants = (1..4).map { idx ->
            TournamentParticipantEntity(
                tournamentId = tournamentId,
                playerId = "p-$idx",
                playerName = "Player $idx",
                seed = idx,
                totalPoints = idx * 10,
                totalDiff = idx * 2,
                matchesPlayed = 2,
                matchesWon = 1,
                rank = idx
            )
        }
        tournamentDao.insertParticipants(participants)

        val round1 = TournamentRoundEntity(
            id = "r-1",
            tournamentId = tournamentId,
            roundNumber = 1,
            status = "COMPLETED"
        )
        val round2 = TournamentRoundEntity(
            id = "r-2",
            tournamentId = tournamentId,
            roundNumber = 2,
            status = "IN_PROGRESS"
        )
        tournamentDao.insertRounds(listOf(round1, round2))

        val matches = listOf(
            TournamentMatchEntity(
                id = "m-101",
                roundId = "r-1",
                tournamentId = tournamentId,
                courtNumber = 1,
                team1Player1Id = "p-1",
                team1Player2Id = "p-2",
                team2Player1Id = "p-3",
                team2Player2Id = "p-4",
                team1Score = 18,
                team2Score = 14,
                isCompleted = true
            ),
            TournamentMatchEntity(
                id = "m-102",
                roundId = "r-2",
                tournamentId = tournamentId,
                courtNumber = 1,
                team1Player1Id = "p-1",
                team1Player2Id = "p-3",
                team2Player1Id = "p-2",
                team2Player2Id = "p-4",
                team1Score = 0,
                team2Score = 0,
                isCompleted = false
            )
        )
        tournamentDao.insertMatches(matches)

        // Verify entities exist prior to deletion
        assertNotNull(tournamentDao.getTournamentById(tournamentId))
        assertEquals(4, tournamentDao.getParticipants(tournamentId).size)
        assertEquals(2, tournamentDao.getRounds(tournamentId).size)
        assertEquals(2, tournamentDao.getTournamentMatches(tournamentId).size)

        // Delete Tournament -> SQLite foreign key cascades to Participants, Rounds, and Matches
        tournamentDao.deleteTournament(tournament)

        assertNull(tournamentDao.getTournamentById(tournamentId))
        assertEquals(0, tournamentDao.getParticipants(tournamentId).size)
        assertEquals(0, tournamentDao.getRounds(tournamentId).size)
        assertEquals(0, tournamentDao.getTournamentMatches(tournamentId).size)
        assertNull(tournamentDao.getMatchById("m-101"))
        assertNull(tournamentDao.getMatchById("m-102"))
    }

    // --- 5. Match and Confirmation Cascading Deletion ---

    @Test
    fun testMatchConfirmationCascadingDeletion() = runTest {
        val match = MatchEntity(
            id = "match-casc-1",
            matchType = "RANKED",
            scoringMode = "TRADITIONAL",
            team1Player1Id = "p1",
            team1Player2Id = "p2",
            team2Player1Id = "p3",
            team2Player2Id = "p4",
            set1T1 = 6,
            set1T2 = 4,
            set2T1 = 6,
            set2T2 = 3,
            winningTeam = 1,
            submittedByUserId = "p1",
            confirmationStatus = "PENDING"
        )
        matchDao.insertMatch(match)

        val confirmation = MatchConfirmationEntity(
            id = "conf-1",
            matchId = "match-casc-1",
            userId = "p3",
            status = "CONFIRMED",
            confirmedAt = 123456L
        )
        matchDao.insertConfirmation(confirmation)

        val confirmationsBefore = matchDao.getConfirmationsForMatch("match-casc-1").first()
        assertEquals(1, confirmationsBefore.size)
        assertEquals("conf-1", confirmationsBefore[0].id)

        // Delete Match -> cascades to confirmations
        matchDao.deleteMatch(match)

        val confirmationsAfter = matchDao.getConfirmationsForMatch("match-casc-1").first()
        assertTrue(confirmationsAfter.isEmpty())
        assertNull(matchDao.getConfirmationById("conf-1"))
    }

    // --- 6. Group Cascading Deletion & Leaderboard Ordering ---

    @Test
    fun testGroupCascadingDeletionAndLeaderboard() = runTest {
        val group = GroupEntity(
            id = "group-10",
            name = "Valencia Padel Squad",
            description = "Casual & Competitive weekend players",
            createdById = "u-owner"
        )
        groupDao.insertGroup(group)

        val members = listOf(
            GroupMemberEntity(
                groupId = "group-10",
                userId = "u-1",
                role = "MEMBER",
                groupElo = 1450.0,
                groupPoints = 80,
                tournamentsWon = 1
            ),
            GroupMemberEntity(
                groupId = "group-10",
                userId = "u-2",
                role = "ADMIN",
                groupElo = 1920.0,
                groupPoints = 220,
                tournamentsWon = 5
            ),
            GroupMemberEntity(
                groupId = "group-10",
                userId = "u-3",
                role = "MEMBER",
                groupElo = 1680.0,
                groupPoints = 140,
                tournamentsWon = 2
            )
        )
        groupDao.insertMembers(members)

        val leaderboard = groupDao.getGroupLeaderboard("group-10").first()
        assertEquals(3, leaderboard.size)
        // Leaderboard must be sorted by groupElo DESC
        assertEquals("u-2", leaderboard[0].userId)
        assertEquals(1920.0, leaderboard[0].groupElo, 0.001)
        assertEquals("u-3", leaderboard[1].userId)
        assertEquals(1680.0, leaderboard[1].groupElo, 0.001)
        assertEquals("u-1", leaderboard[2].userId)
        assertEquals(1450.0, leaderboard[2].groupElo, 0.001)

        // Delete Group -> cascade delete members
        groupDao.deleteGroup(group)

        val emptyLeaderboard = groupDao.getGroupLeaderboard("group-10").first()
        assertTrue(emptyLeaderboard.isEmpty())
        assertNull(groupDao.getMember("group-10", "u-2"))
    }

    // --- 7. Tournament Match Score Update ---

    @Test
    fun testUpdateMatchScore() = runTest {
        val tournament = TournamentEntity(
            id = "t-score-test",
            name = "Score Test Americano",
            totalCourts = 1,
            createdById = "c-1"
        )
        tournamentDao.insertTournament(tournament)

        val round = TournamentRoundEntity(
            id = "r-score-test",
            tournamentId = "t-score-test",
            roundNumber = 1
        )
        tournamentDao.insertRounds(listOf(round))

        val match = TournamentMatchEntity(
            id = "m-score-1",
            roundId = "r-score-test",
            tournamentId = "t-score-test",
            courtNumber = 1,
            team1Player1Id = "p1",
            team1Player2Id = "p2",
            team2Player1Id = "p3",
            team2Player2Id = "p4",
            team1Score = 0,
            team2Score = 0,
            isCompleted = false
        )
        tournamentDao.insertMatches(listOf(match))

        // Update score
        tournamentDao.updateMatchScore("m-score-1", 21, 11)

        val updated = tournamentDao.getMatchById("m-score-1")
        assertNotNull(updated)
        assertEquals(21, updated?.team1Score)
        assertEquals(11, updated?.team2Score)
        assertTrue(updated?.isCompleted == true)
    }

    // --- 8. Match Confirmation Status Flow ---

    @Test
    fun testPendingMatchesAndStatusUpdate() = runTest {
        val pendingMatch = MatchEntity(
            id = "match-pend-1",
            team1Player1Id = "p1",
            team2Player1Id = "p2",
            set1T1 = 6,
            set1T2 = 2,
            set2T1 = 6,
            set2T2 = 4,
            winningTeam = 1,
            submittedByUserId = "p1",
            confirmationStatus = "PENDING",
            playedAt = 5000L
        )
        val confirmedMatch = MatchEntity(
            id = "match-conf-2",
            team1Player1Id = "p3",
            team2Player1Id = "p4",
            set1T1 = 6,
            set1T2 = 0,
            set2T1 = 6,
            set2T2 = 1,
            winningTeam = 1,
            submittedByUserId = "p3",
            confirmationStatus = "CONFIRMED",
            playedAt = 6000L
        )
        matchDao.insertMatches(listOf(pendingMatch, confirmedMatch))

        val pendingList = matchDao.getPendingMatches().first()
        assertEquals(1, pendingList.size)
        assertEquals("match-pend-1", pendingList[0].id)

        // Confirm the match
        matchDao.updateConfirmationStatus("match-pend-1", "CONFIRMED")

        val emptyPendingList = matchDao.getPendingMatches().first()
        assertTrue(emptyPendingList.isEmpty())
    }

    // --- 9. Bidirectional Entity Mappers ---

    @Test
    fun testEntityMappersBidirectional() {
        val userDomain = User(
            id = "usr-map-1",
            name = "Paquito Navarro",
            email = "paquito@padel.es",
            preferredSide = CourtSide.REVES,
            level = 6.90
        )
        val userEntity = userDomain.toEntity()
        assertEquals("usr-map-1", userEntity.id)
        assertEquals("REVES", userEntity.preferredSide)
        assertEquals(userDomain, userEntity.toDomain())

        val tournamentDomain = Tournament(
            id = "trn-map-1",
            name = "Madrid Open Americano",
            format = TournamentFormat.MEXICANO,
            status = TournamentStatus.IN_PROGRESS,
            totalCourts = 3,
            createdById = "u-1"
        )
        val tournamentEntity = tournamentDomain.toEntity()
        assertEquals("MEXICANO", tournamentEntity.format)
        assertEquals("IN_PROGRESS", tournamentEntity.status)
        assertEquals(tournamentDomain, tournamentEntity.toDomain())

        val matchDomain = Match(
            id = "mtc-map-1",
            team1Player1Id = "p1",
            team2Player1Id = "p2",
            set1T1 = 6,
            set1T2 = 3,
            set2T1 = 7,
            set2T2 = 5,
            winningTeam = 1,
            submittedByUserId = "p1"
        )
        val matchEntity = matchDomain.toEntity()
        assertEquals("mtc-map-1", matchEntity.id)
        assertEquals(matchDomain, matchEntity.toDomain())

        val participantDomain = TournamentParticipant(
            tournamentId = "trn-map-1",
            playerId = "p-1",
            playerName = "Ale Galán",
            seed = 1,
            totalPoints = 32,
            totalDiff = 12,
            matchesPlayed = 4,
            matchesWon = 3,
            rank = 1
        )
        assertEquals(participantDomain, participantDomain.toEntity().toDomain())

        val tournamentMatchDomain = TournamentMatch(
            id = "tm-map-1",
            roundId = "r-map-1",
            tournamentId = "trn-map-1",
            courtNumber = 1,
            team1Player1Id = "p1",
            team1Player2Id = "p2",
            team2Player1Id = "p3",
            team2Player2Id = "p4",
            team1Score = 21,
            team2Score = 15,
            isCompleted = true
        )
        assertEquals(tournamentMatchDomain, tournamentMatchDomain.toEntity().toDomain())
    }
}
