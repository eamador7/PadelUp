package com.padelscoring.domain

import com.padelscoring.domain.model.Tournament
import com.padelscoring.domain.model.TournamentFormat
import com.padelscoring.domain.model.TournamentParticipant
import com.padelscoring.domain.model.TournamentStatus
import com.padelscoring.domain.tournament.AmericanoEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class AmericanoEngineTest {

    private fun canonicalPair(a: String, b: String): Pair<String, String> =
        if (a < b) a to b else b to a

    @Test
    fun test4PlayerAmericanoScheduleHasZeroDuplicatePartnerships() {
        val players = listOf("p1", "p2", "p3", "p4")
        val schedule = AmericanoEngine.generateSchedule(players, totalCourts = 1)

        assertEquals("4 players should generate 3 rounds", 3, schedule.size)

        val partnerPairs = mutableListOf<Pair<String, String>>()
        for (round in schedule) {
            assertEquals(1, round.matches.size)
            val match = round.matches.first()

            val courtPlayers = listOf(
                match.team1Player1Id,
                match.team1Player2Id,
                match.team2Player1Id,
                match.team2Player2Id
            )
            assertEquals("All 4 players must play in each match", 4, courtPlayers.distinct().size)

            partnerPairs.add(canonicalPair(match.team1Player1Id, match.team1Player2Id))
            partnerPairs.add(canonicalPair(match.team2Player1Id, match.team2Player2Id))
        }

        assertEquals("Total partnerships must equal 6", 6, partnerPairs.size)
        assertEquals("Every pair must be unique (zero duplicate partnerships)", 6, partnerPairs.distinct().size)
    }

    @Test
    fun test8PlayerAmericanoScheduleHasZeroDuplicatePartnerships() {
        val players = (1..8).map { "player_$it" }
        val schedule = AmericanoEngine.generateSchedule(players, totalCourts = 2)

        assertEquals("8 players should generate 7 rounds", 7, schedule.size)

        val partnerPairs = mutableListOf<Pair<String, String>>()
        for (round in schedule) {
            assertEquals("2 courts per round", 2, round.matches.size)
            val roundPlayers = mutableListOf<String>()

            for (match in round.matches) {
                assertNotEquals("Player cannot partner with self", match.team1Player1Id, match.team1Player2Id)
                assertNotEquals("Player cannot partner with self", match.team2Player1Id, match.team2Player2Id)

                partnerPairs.add(canonicalPair(match.team1Player1Id, match.team1Player2Id))
                partnerPairs.add(canonicalPair(match.team2Player1Id, match.team2Player2Id))

                roundPlayers.addAll(listOf(match.team1Player1Id, match.team1Player2Id, match.team2Player1Id, match.team2Player2Id))
            }

            assertEquals("All 8 players must play in every round", 8, roundPlayers.distinct().size)
        }

        // Total 2 courts * 2 teams * 7 rounds = 28 partnerships
        // 8 choose 2 = 28
        assertEquals(28, partnerPairs.size)
        assertEquals("Zero duplicate partnerships across all 7 rounds", 28, partnerPairs.distinct().size)
    }

    @Test
    fun test12PlayerAmericanoScheduleHasZeroDuplicatePartnerships() {
        val players = (1..12).map { "player_$it" }
        val schedule = AmericanoEngine.generateSchedule(players, totalCourts = 3)

        assertEquals("12 players should generate 11 rounds", 11, schedule.size)

        val partnerPairs = mutableListOf<Pair<String, String>>()
        for (round in schedule) {
            assertEquals("3 courts per round", 3, round.matches.size)
            for (match in round.matches) {
                partnerPairs.add(canonicalPair(match.team1Player1Id, match.team1Player2Id))
                partnerPairs.add(canonicalPair(match.team2Player1Id, match.team2Player2Id))
            }
        }

        // 12 choose 2 = 66
        assertEquals(66, partnerPairs.size)
        assertEquals("Zero duplicate partnerships for 12 players across 11 rounds", 66, partnerPairs.distinct().size)
    }

    @Test
    fun test16PlayerAmericanoScheduleHasZeroDuplicatePartnerships() {
        val players = (1..16).map { "player_$it" }
        val schedule = AmericanoEngine.generateSchedule(players, totalCourts = 4)

        assertEquals("16 players should generate 15 rounds", 15, schedule.size)

        val partnerPairs = mutableListOf<Pair<String, String>>()
        for (round in schedule) {
            assertEquals("4 courts per round", 4, round.matches.size)
            for (match in round.matches) {
                partnerPairs.add(canonicalPair(match.team1Player1Id, match.team1Player2Id))
                partnerPairs.add(canonicalPair(match.team2Player1Id, match.team2Player2Id))
            }
        }

        // 16 choose 2 = 120
        assertEquals(120, partnerPairs.size)
        assertEquals("Zero duplicate partnerships for 16 players across 15 rounds", 120, partnerPairs.distinct().size)
    }

    @Test
    fun test5PlayerIrregularScheduleBalancesByesAndEliminatesDuplicates() {
        val players = listOf("p1", "p2", "p3", "p4", "p5")
        val schedule = AmericanoEngine.generateSchedule(players, totalCourts = 1, totalRounds = 5)

        assertEquals(5, schedule.size)

        val byeCounts = players.associateWith { 0 }.toMutableMap()
        val partnerPairs = mutableListOf<Pair<String, String>>()

        for (round in schedule) {
            assertEquals(1, round.matches.size)
            val match = round.matches.first()

            val playing = listOf(
                match.team1Player1Id,
                match.team1Player2Id,
                match.team2Player1Id,
                match.team2Player2Id
            )
            assertEquals("4 distinct players per match", 4, playing.distinct().size)

            val sittingOut = players.filter { it !in playing }
            assertEquals(1, sittingOut.size)
            byeCounts[sittingOut.first()] = byeCounts[sittingOut.first()]!! + 1

            partnerPairs.add(canonicalPair(match.team1Player1Id, match.team1Player2Id))
            partnerPairs.add(canonicalPair(match.team2Player1Id, match.team2Player2Id))
        }

        // Each player rests exactly 1 round: Var(Byes) = 0 <= 1
        for (p in players) {
            assertEquals("Each player must sit out exactly 1 round", 1, byeCounts[p])
        }

        // 5 rounds * 2 teams = 10 partnerships
        // 5 choose 2 = 10
        assertEquals(10, partnerPairs.size)
        assertEquals("Zero duplicate partnerships for 5 players in 5 rounds", 10, partnerPairs.distinct().size)
    }

    @Test
    fun testIrregularCountsMaintainBalancedByes() {
        for (n in listOf(6, 7, 9, 10)) {
            val players = (1..n).map { "p$it" }
            val courts = minOf(2, n / 4)
            val rounds = 5
            val schedule = AmericanoEngine.generateSchedule(players, totalCourts = courts, totalRounds = rounds)

            assertEquals(rounds, schedule.size)

            val byeCounts = players.associateWith { 0 }.toMutableMap()
            for (round in schedule) {
                val roundPlayers = mutableListOf<String>()
                for (match in round.matches) {
                    roundPlayers.addAll(listOf(match.team1Player1Id, match.team1Player2Id, match.team2Player1Id, match.team2Player2Id))
                }
                assertEquals("No duplicate players on court in same round", roundPlayers.size, roundPlayers.distinct().size)

                val byes = players.filter { it !in roundPlayers }
                for (b in byes) {
                    byeCounts[b] = byeCounts[b]!! + 1
                }
            }

            val maxByes = byeCounts.values.maxOrNull() ?: 0
            val minByes = byeCounts.values.minOrNull() ?: 0
            assertTrue("Bye variance must be <= 1 for N=$n (max=$maxByes, min=$minByes)", maxByes - minByes <= 1)
        }
    }

    @Test
    fun testScoreUpdatePointConservation() {
        val players = (1..4).map { "p$it" }
        val tournament = Tournament(
            id = "tourn_1",
            name = "Summer Americano",
            format = TournamentFormat.AMERICANO,
            targetPoints = 32,
            totalCourts = 1,
            status = TournamentStatus.IN_PROGRESS,
            createdById = "admin"
        )
        val schedule = AmericanoEngine.generateSchedule(players, totalCourts = 1, tournamentId = tournament.id)
        val round = schedule.first()
        val matchId = round.matches.first().id

        // Valid score: 18 + 14 = 32
        val updatedRound = AmericanoEngine.updateMatchScore(tournament, round, matchId, 18, 14)
        val updatedMatch = updatedRound.matches.first { it.id == matchId }
        assertTrue(updatedMatch.isCompleted)
        assertEquals(18, updatedMatch.team1Score)
        assertEquals(14, updatedMatch.team2Score)

        // Invalid score: 18 + 10 != 32 throws IllegalArgumentException
        try {
            AmericanoEngine.updateMatchScore(tournament, round, matchId, 18, 10)
            fail("Expected IllegalArgumentException for score sum mismatch")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("must equal tournament target points"))
        }

        // Negative score throws IllegalArgumentException
        try {
            AmericanoEngine.updateMatchScore(tournament, round, matchId, -1, 33)
            fail("Expected IllegalArgumentException for negative score")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("non-negative"))
        }
    }

    @Test
    fun testCalculateLeaderboardRankingsAndTieBreakers() {
        val tournamentId = "t1"
        val participants = listOf(
            TournamentParticipant(tournamentId, "p1", "Alice", seed = 1),
            TournamentParticipant(tournamentId, "p2", "Bob", seed = 2),
            TournamentParticipant(tournamentId, "p3", "Charlie", seed = 3),
            TournamentParticipant(tournamentId, "p4", "David", seed = 4)
        )

        val schedule = AmericanoEngine.generateSchedule(
            listOf("p1", "p2", "p3", "p4"),
            totalCourts = 1,
            tournamentId = tournamentId
        )

        // Round 1: (p4, p1) vs (p2, p3) -> 20 - 12
        val r1m = schedule[0].matches[0].copy(
            team1Score = 20,
            team2Score = 12,
            isCompleted = true
        )
        // Round 2: (p4, p2) vs (p3, p1) -> 16 - 16
        val r2m = schedule[1].matches[0].copy(
            team1Score = 16,
            team2Score = 16,
            isCompleted = true
        )

        val leaderboard = AmericanoEngine.calculateLeaderboard(participants, listOf(r1m, r2m))

        assertEquals(4, leaderboard.size)

        // Player 4: 20 + 16 = 36 points, diff = (20-12) + (16-16) = +8, 1 win
        val p4 = leaderboard.first { it.playerId == "p4" }
        assertEquals(36, p4.totalPoints)
        assertEquals(8, p4.totalDiff)
        assertEquals(1, p4.matchesWon)
        assertEquals(2, p4.matchesPlayed)
        assertEquals(2, p4.rank)

        // Player 1: 20 + 16 = 36 points, diff = +8, 1 win
        val p1 = leaderboard.first { it.playerId == "p1" }
        assertEquals(36, p1.totalPoints)
        assertEquals(8, p1.totalDiff)
        assertEquals(1, p1.matchesWon)

        // Since p1 and p4 have identical points, diff, and wins, seed 1 (p1) beats seed 4 (p4)
        assertEquals(1, leaderboard[0].rank)
        assertEquals("p1", leaderboard[0].playerId)
        assertEquals(2, leaderboard[1].rank)
        assertEquals("p4", leaderboard[1].playerId)

        // All ranks must be sequential 1..N
        assertEquals(listOf(1, 2, 3, 4), leaderboard.map { it.rank })
    }
}
