package com.padelscoring.domain

import com.padelscoring.domain.model.Match
import com.padelscoring.domain.model.User
import com.padelscoring.domain.rating.PadelDynamicRatingCalculator
import com.padelscoring.domain.rating.PadelDynamicRatingCalculator.PlayerRating
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PadelDynamicRatingCalculatorTest {

    @Test
    fun testRatingIncreasesOnWinAndDecreasesOnLoss() {
        val t1p1 = PlayerRating("p1", level = 2.50, confidence = 0.20)
        val t1p2 = PlayerRating("p2", level = 2.50, confidence = 0.20)
        val t2p1 = PlayerRating("p3", level = 2.50, confidence = 0.20)
        val t2p2 = PlayerRating("p4", level = 2.50, confidence = 0.20)

        // Team 1 wins 6-4, 6-4 (12 games to 8)
        val results = PadelDynamicRatingCalculator.calculateMatchRating(
            team1Player1 = t1p1,
            team1Player2 = t1p2,
            team2Player1 = t2p1,
            team2Player2 = t2p2,
            team1GamesWon = 12,
            team2GamesWon = 8,
            winningTeam = 1
        )

        val r1 = results["p1"]!!
        val r2 = results["p2"]!!
        val r3 = results["p3"]!!
        val r4 = results["p4"]!!

        // Winners must gain rating
        assertTrue("Winner p1 level should increase", r1.newLevel > r1.oldLevel)
        assertTrue("Winner p1 delta should be positive", r1.delta > 0.0)
        assertTrue("Winner p2 level should increase", r2.newLevel > r2.oldLevel)
        assertTrue("Winner p2 delta should be positive", r2.delta > 0.0)

        // Losers must lose rating
        assertTrue("Loser p3 level should decrease", r3.newLevel < r3.oldLevel)
        assertTrue("Loser p3 delta should be negative", r3.delta < 0.0)
        assertTrue("Loser p4 level should decrease", r4.newLevel < r4.oldLevel)
        assertTrue("Loser p4 delta should be negative", r4.delta < 0.0)

        // Confidence should increment by 0.04 (0.20 -> 0.24)
        assertEquals(0.24, r1.newConfidence, 0.001)
        assertEquals(0.24, r3.newConfidence, 0.001)
    }

    @Test
    fun testScoreBlowoutProducesLargerDeltaThanCloseTiebreaker() {
        val t1p1 = PlayerRating("p1", level = 2.50, confidence = 0.50)
        val t1p2 = PlayerRating("p2", level = 2.50, confidence = 0.50)
        val t2p1 = PlayerRating("p3", level = 2.50, confidence = 0.50)
        val t2p2 = PlayerRating("p4", level = 2.50, confidence = 0.50)

        // Blowout: 6-0, 6-0 (12 to 0 games)
        val blowoutResults = PadelDynamicRatingCalculator.calculateMatchRating(
            team1Player1 = t1p1,
            team1Player2 = t1p2,
            team2Player1 = t2p1,
            team2Player2 = t2p2,
            team1GamesWon = 12,
            team2GamesWon = 0,
            winningTeam = 1
        )

        // Close match: 7-6, 7-6 (14 to 12 games)
        val closeResults = PadelDynamicRatingCalculator.calculateMatchRating(
            team1Player1 = t1p1,
            team1Player2 = t1p2,
            team2Player1 = t2p1,
            team2Player2 = t2p2,
            team1GamesWon = 14,
            team2GamesWon = 12,
            winningTeam = 1
        )

        val blowoutDelta = blowoutResults["p1"]!!.delta
        val closeDelta = closeResults["p1"]!!.delta

        assertTrue("Blowout delta ($blowoutDelta) must exceed close match delta ($closeDelta)", blowoutDelta > closeDelta)
    }

    @Test
    fun testAsymmetricPartnerRatingsGiveLargerShareToLowerRatedTeammate() {
        // Lower rated player (2.00) + Higher rated player (3.00) vs (2.50 + 2.50)
        val lowerPlayer = PlayerRating("p_low", level = 2.00, confidence = 0.50)
        val higherPlayer = PlayerRating("p_high", level = 3.00, confidence = 0.50)
        val opp1 = PlayerRating("opp1", level = 2.50, confidence = 0.50)
        val opp2 = PlayerRating("opp2", level = 2.50, confidence = 0.50)

        val results = PadelDynamicRatingCalculator.calculateMatchRating(
            team1Player1 = lowerPlayer,
            team1Player2 = higherPlayer,
            team2Player1 = opp1,
            team2Player2 = opp2,
            team1GamesWon = 12,
            team2GamesWon = 6,
            winningTeam = 1
        )

        val lowDelta = results["p_low"]!!.delta
        val highDelta = results["p_high"]!!.delta

        assertTrue("Lower rated player delta ($lowDelta) must exceed higher rated partner delta ($highDelta)", lowDelta > highDelta)
    }

    @Test
    fun testConfidenceDampeningProvisionalPlayerHasHigherVolatility() {
        // Provisional player (C=0.20) vs Established player (C=0.90)
        val provisionalPlayer = PlayerRating("prov", level = 2.50, confidence = 0.20)
        val establishedPlayer = PlayerRating("estab", level = 2.50, confidence = 0.90)

        val partner = PlayerRating("part", level = 2.50, confidence = 0.50)
        val opp1 = PlayerRating("o1", level = 2.50, confidence = 0.50)
        val opp2 = PlayerRating("o2", level = 2.50, confidence = 0.50)

        val resProv = PadelDynamicRatingCalculator.calculateMatchRating(
            team1Player1 = provisionalPlayer,
            team1Player2 = partner,
            team2Player1 = opp1,
            team2Player2 = opp2,
            team1GamesWon = 12,
            team2GamesWon = 6,
            winningTeam = 1
        )

        val resEstab = PadelDynamicRatingCalculator.calculateMatchRating(
            team1Player1 = establishedPlayer,
            team1Player2 = partner,
            team2Player1 = opp1,
            team2Player2 = opp2,
            team1GamesWon = 12,
            team2GamesWon = 6,
            winningTeam = 1
        )

        val provDelta = resProv["prov"]!!.delta
        val estabDelta = resEstab["estab"]!!.delta

        assertTrue("Provisional player delta ($provDelta) must be higher than established player delta ($estabDelta)", provDelta > estabDelta)
    }

    @Test
    fun testBoundaryClampingStrictlyEnforcesOneToSevenRange() {
        // Upper bound: player at 6.95 defeats equal top opponents 12-0
        val topPlayer = PlayerRating("top", level = 6.95, confidence = 0.20)
        val partner = PlayerRating("part", level = 6.95, confidence = 0.20)
        val topOpp1 = PlayerRating("top_o1", level = 6.95, confidence = 0.50)
        val topOpp2 = PlayerRating("top_o2", level = 6.95, confidence = 0.50)

        val maxResults = PadelDynamicRatingCalculator.calculateMatchRating(
            team1Player1 = topPlayer,
            team1Player2 = partner,
            team2Player1 = topOpp1,
            team2Player2 = topOpp2,
            team1GamesWon = 12,
            team2GamesWon = 0,
            winningTeam = 1
        )
        val topResult = maxResults["top"]!!
        assertTrue("Rating cannot exceed 7.00 (was ${topResult.newLevel})", topResult.newLevel <= 7.00)
        assertEquals(7.00, topResult.newLevel, 0.001)

        // Lower bound: player at 1.05 loses a blowout to equal lowest opponents
        val lowPlayer = PlayerRating("bottom", level = 1.05, confidence = 0.20)
        val lowPartner = PlayerRating("part_low", level = 1.05, confidence = 0.20)
        val lowOpp1 = PlayerRating("low_o1", level = 1.05, confidence = 0.50)
        val lowOpp2 = PlayerRating("low_o2", level = 1.05, confidence = 0.50)

        val minResults = PadelDynamicRatingCalculator.calculateMatchRating(
            team1Player1 = lowPlayer,
            team1Player2 = lowPartner,
            team2Player1 = lowOpp1,
            team2Player2 = lowOpp2,
            team1GamesWon = 0,
            team2GamesWon = 12,
            winningTeam = 2
        )
        val lowResult = minResults["bottom"]!!
        assertTrue("Rating cannot drop below 1.00 (was ${lowResult.newLevel})", lowResult.newLevel >= 1.00)
        assertEquals(1.00, lowResult.newLevel, 0.001)
    }

    @Test
    fun testOnboardingInitialLevelCalculation() {
        // Min scores (all 0) evaluate to 1.50 with confidence 0.20
        val (minLevel, minConf) = PadelDynamicRatingCalculator.calculateInitialLevel(listOf(0, 0, 0, 0, 0))
        assertEquals(1.50, minLevel, 0.001)
        assertEquals(0.20, minConf, 0.001)

        // Max scores (all 3) evaluate to 3.25 with confidence 0.20
        val (maxLevel, maxConf) = PadelDynamicRatingCalculator.calculateInitialLevel(listOf(3, 3, 3, 3, 3))
        assertEquals(3.25, maxLevel, 0.001)
        assertEquals(0.20, maxConf, 0.001)

        // Intermediate answers fall strictly between 1.50 and 3.25
        val (midLevel, midConf) = PadelDynamicRatingCalculator.calculateInitialLevel(listOf(1, 2, 1, 2, 1))
        assertTrue("Mid level must be > 1.50", midLevel > 1.50)
        assertTrue("Mid level must be < 3.25", midLevel < 3.25)
        assertEquals(0.20, midConf, 0.001)

        // Empty list defaults to base 1.50
        val (emptyLevel, emptyConf) = PadelDynamicRatingCalculator.calculateInitialLevel(emptyList())
        assertEquals(1.50, emptyLevel, 0.001)
        assertEquals(0.20, emptyConf, 0.001)
    }

    @Test
    fun testMatchModelOverloadIntegration() {
        val user1 = User("u1", "User 1", level = 2.50, levelConfidence = 0.20)
        val user2 = User("u2", "User 2", level = 2.50, levelConfidence = 0.20)
        val user3 = User("u3", "User 3", level = 2.50, levelConfidence = 0.20)
        val user4 = User("u4", "User 4", level = 2.50, levelConfidence = 0.20)
        val users = listOf(user1, user2, user3, user4).associateBy { it.id }

        val match = Match(
            id = "m1",
            team1Player1Id = "u1",
            team1Player2Id = "u2",
            team2Player1Id = "u3",
            team2Player2Id = "u4",
            set1T1 = 6,
            set1T2 = 3,
            set2T1 = 6,
            set2T2 = 4,
            set3T1 = null,
            set3T2 = null,
            winningTeam = 1,
            submittedByUserId = "u1"
        )

        val results = PadelDynamicRatingCalculator.calculateMatchRating(match, users)

        assertEquals(4, results.size)
        assertTrue("Winner u1 must have increased level", results["u1"]!!.newLevel > 2.50)
        assertTrue("Winner u2 must have increased level", results["u2"]!!.newLevel > 2.50)
        assertTrue("Loser u3 must have decreased level", results["u3"]!!.newLevel < 2.50)
        assertTrue("Loser u4 must have decreased level", results["u4"]!!.newLevel < 2.50)
    }

    @Test
    fun testAsymmetricRatingShieldsWeakerPartnerOnLoss() {
        // Team 1: p1 (stronger: 4.0), p2 (weaker: 2.0)
        // Team 2: p3 (3.0), p4 (3.0)
        val t1p1 = PlayerRating("p1", level = 4.00, confidence = 0.50)
        val t1p2 = PlayerRating("p2", level = 2.00, confidence = 0.50)
        val t2p1 = PlayerRating("p3", level = 3.00, confidence = 0.50)
        val t2p2 = PlayerRating("p4", level = 3.00, confidence = 0.50)

        // Team 2 wins (Team 1 loses)
        val results = PadelDynamicRatingCalculator.calculateMatchRating(
            team1Player1 = t1p1,
            team1Player2 = t1p2,
            team2Player1 = t2p1,
            team2Player2 = t2p2,
            team1GamesWon = 8,
            team2GamesWon = 12,
            winningTeam = 2
        )

        val rStronger = results["p1"]!!
        val rWeaker = results["p2"]!!

        // Both lose rating (deltas are negative)
        assertTrue(rStronger.delta < 0)
        assertTrue(rWeaker.delta < 0)

        // The stronger partner must absorb more loss (more negative delta), shielding the weaker partner
        assertTrue(
            "Stronger player (${rStronger.delta}) should lose more or equal to weaker player (${rWeaker.delta})",
            rStronger.delta <= rWeaker.delta
        )
    }
}
