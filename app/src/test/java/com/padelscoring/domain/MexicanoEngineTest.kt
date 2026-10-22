package com.padelscoring.domain

import com.padelscoring.domain.model.TournamentMatch
import com.padelscoring.domain.model.TournamentParticipant
import com.padelscoring.domain.model.TournamentRound
import com.padelscoring.domain.tournament.MexicanoEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MexicanoEngineTest {

    private fun canonicalPair(a: String, b: String): Pair<String, String> =
        if (a < b) a to b else b to a

    @Test
    fun testTieredCourtAssignmentAndStandardParityPairing() {
        val tournamentId = "mex_1"
        val participants = (1..8).map { rank ->
            TournamentParticipant(
                tournamentId = tournamentId,
                playerId = "player_$rank",
                playerName = "Player $rank",
                rank = rank,
                totalPoints = (9 - rank) * 10
            )
        }

        val round1 = MexicanoEngine.generateNextRound(
            tournamentId = tournamentId,
            roundNumber = 1,
            rankedParticipants = participants,
            pastRounds = emptyList(),
            totalCourts = 2
        )

        assertEquals(2, round1.matches.size)

        // Court 1: Ranks 1 to 4 -> Standard parity: 1+4 vs 2+3
        val court1Match = round1.matches.first { it.courtNumber == 1 }
        val court1Players = listOf(
            court1Match.team1Player1Id,
            court1Match.team1Player2Id,
            court1Match.team2Player1Id,
            court1Match.team2Player2Id
        ).toSet()
        assertEquals(setOf("player_1", "player_2", "player_3", "player_4"), court1Players)

        val c1t1 = setOf(court1Match.team1Player1Id, court1Match.team1Player2Id)
        val c1t2 = setOf(court1Match.team2Player1Id, court1Match.team2Player2Id)
        assertEquals(setOf("player_1", "player_4"), c1t1)
        assertEquals(setOf("player_2", "player_3"), c1t2)

        // Court 2: Ranks 5 to 8 -> Standard parity: 5+8 vs 6+7
        val court2Match = round1.matches.first { it.courtNumber == 2 }
        val court2Players = listOf(
            court2Match.team1Player1Id,
            court2Match.team1Player2Id,
            court2Match.team2Player1Id,
            court2Match.team2Player2Id
        ).toSet()
        assertEquals(setOf("player_5", "player_6", "player_7", "player_8"), court2Players)

        val c2t1 = setOf(court2Match.team1Player1Id, court2Match.team1Player2Id)
        val c2t2 = setOf(court2Match.team2Player1Id, court2Match.team2Player2Id)
        assertEquals(setOf("player_5", "player_8"), c2t1)
        assertEquals(setOf("player_6", "player_7"), c2t2)
    }

    @Test
    fun testPartnerCollisionAvoidanceSwapsToOption2() {
        val tournamentId = "mex_collision"
        val participants = (1..4).map { rank ->
            TournamentParticipant(
                tournamentId = tournamentId,
                playerId = "p$rank",
                playerName = "P$rank",
                rank = rank
            )
        }

        // Simulate that in Round 1, p1 partnered with p4 and p2 partnered with p3
        val pastRound1 = TournamentRound(
            id = "r1",
            tournamentId = tournamentId,
            roundNumber = 1,
            matches = listOf(
                TournamentMatch(
                    id = "m1",
                    roundId = "r1",
                    tournamentId = tournamentId,
                    courtNumber = 1,
                    team1Player1Id = "p1",
                    team1Player2Id = "p4",
                    team2Player1Id = "p2",
                    team2Player2Id = "p3",
                    team1Score = 18,
                    team2Score = 14,
                    isCompleted = true
                )
            )
        )

        // In Round 2, p1 and p4 must NOT partner again; engine must swap to Option 2: 1+3 vs 2+4
        val round2 = MexicanoEngine.generateNextRound(
            tournamentId = tournamentId,
            roundNumber = 2,
            rankedParticipants = participants,
            pastRounds = listOf(pastRound1),
            totalCourts = 1
        )

        val match = round2.matches.first()
        val t1 = setOf(match.team1Player1Id, match.team1Player2Id)
        val t2 = setOf(match.team2Player1Id, match.team2Player2Id)

        assertEquals("Option 2: p1 must partner with p3", setOf("p1", "p3"), t1)
        assertEquals("Option 2: p2 must partner with p4", setOf("p2", "p4"), t2)
    }

    @Test
    fun testPartnerCollisionAvoidanceSwapsToOption3WhenOption1And2HaveCollisions() {
        val tournamentId = "mex_collision_3"
        val participants = (1..4).map { rank ->
            TournamentParticipant(
                tournamentId = tournamentId,
                playerId = "p$rank",
                playerName = "P$rank",
                rank = rank
            )
        }

        // Round 1: p1+p4 vs p2+p3
        val pastRound1 = TournamentRound(
            id = "r1",
            tournamentId = tournamentId,
            roundNumber = 1,
            matches = listOf(
                TournamentMatch(
                    id = "m1",
                    roundId = "r1",
                    tournamentId = tournamentId,
                    courtNumber = 1,
                    team1Player1Id = "p1",
                    team1Player2Id = "p4",
                    team2Player1Id = "p2",
                    team2Player2Id = "p3",
                    isCompleted = true
                )
            )
        )
        // Round 2: p1+p3 vs p2+p4
        val pastRound2 = TournamentRound(
            id = "r2",
            tournamentId = tournamentId,
            roundNumber = 2,
            matches = listOf(
                TournamentMatch(
                    id = "m2",
                    roundId = "r2",
                    tournamentId = tournamentId,
                    courtNumber = 1,
                    team1Player1Id = "p1",
                    team1Player2Id = "p3",
                    team2Player1Id = "p2",
                    team2Player2Id = "p4",
                    isCompleted = true
                )
            )
        )

        // In Round 3, Option 1 (1+4) and Option 2 (1+3) have collided.
        // Engine must select Option 3: 1+2 vs 3+4
        val round3 = MexicanoEngine.generateNextRound(
            tournamentId = tournamentId,
            roundNumber = 3,
            rankedParticipants = participants,
            pastRounds = listOf(pastRound1, pastRound2),
            totalCourts = 1
        )

        val match = round3.matches.first()
        val t1 = setOf(match.team1Player1Id, match.team1Player2Id)
        val t2 = setOf(match.team2Player1Id, match.team2Player2Id)

        assertEquals("Option 3: p1 must partner with p2", setOf("p1", "p2"), t1)
        assertEquals("Option 3: p3 must partner with p4", setOf("p3", "p4"), t2)
    }

    @Test
    fun testValidationRequiresAtLeast4Players() {
        val participants = listOf(
            TournamentParticipant("t", "p1", "P1", rank = 1),
            TournamentParticipant("t", "p2", "P2", rank = 2),
            TournamentParticipant("t", "p3", "P3", rank = 3)
        )
        try {
            MexicanoEngine.generateNextRound("t", 1, participants, emptyList(), totalCourts = 1)
            org.junit.Assert.fail("Expected IllegalArgumentException for < 4 participants")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("at least 4 participants"))
        }
    }
}
