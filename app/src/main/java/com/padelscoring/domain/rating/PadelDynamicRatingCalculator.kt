package com.padelscoring.domain.rating

import com.padelscoring.domain.model.Match
import com.padelscoring.domain.model.User
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Playtomic-style Padel Dynamic Rating (PDR 1.00–7.00) mathematical calculator.
 *
 * Implements:
 * 1. Win expectancy using logistic 2v2 rating difference:
 *    E_A = 1.0 / (1.0 + 10.0 ^ ((R_B_mean - R_A_mean) / 1.50))
 * 2. Match margin multiplier:
 *    rho = |G_A - G_B| / (G_A + G_B), M = 0.75 + 0.60 * sqrt(rho)
 * 3. Base team delta:
 *    Delta_R_team = K * (S_A - E_A) * M, where K = 0.35, S_A in {1.0, 0.0}
 * 4. Confidence and gap-weighted asymmetric distribution:
 *    Delta_R_i = Delta_R_team * (1.0 + 0.25 * ((R_team - R_i) / R_team)) * (1.30 - 0.50 * C_i)
 * 5. Strict clamping within [1.00, 7.00] and confidence evolution.
 * 6. Onboarding calibration quiz mapping to [1.50, 3.25] with initial confidence 0.20.
 */
object PadelDynamicRatingCalculator {

    const val K_FACTOR = 0.35
    const val SCALE_FACTOR = 1.50
    const val MIN_RATING = 1.00
    const val MAX_RATING = 7.00
    const val INITIAL_CONFIDENCE = 0.20
    const val CONFIDENCE_STEP = 0.04

    data class PlayerRating(
        val userId: String,
        val level: Double,
        val confidence: Double = INITIAL_CONFIDENCE
    )

    data class RatingResult(
        val userId: String,
        val oldLevel: Double,
        val newLevel: Double,
        val delta: Double,
        val oldConfidence: Double,
        val newConfidence: Double
    )

    /**
     * Maps an onboarding quiz score vector (5 questions, each scored 0 to 3)
     * to a baseline skill level in [1.50, 3.25] with provisional confidence 0.20.
     *
     * @param quizScores List of integer scores representing answers to onboarding questions.
     * @return Pair of (provisionalLevel, initialConfidence).
     */
    fun calculateInitialLevel(quizScores: List<Int>): Pair<Double, Double> {
        if (quizScores.isEmpty()) {
            return Pair(1.50, INITIAL_CONFIDENCE)
        }

        // Discrete weights for the 5 survey questions summing to 1.75 (3.25 - 1.50):
        // 1. Racket sport background (0..3): 0.35 max
        // 2. Play frequency (0..3): 0.40 max
        // 3. Wall rebound comfort (0..3): 0.35 max
        // 4. Overhead proficiency (0..3): 0.35 max
        // 5. Tournament experience (0..3): 0.30 max
        val weights = listOf(0.35, 0.40, 0.35, 0.35, 0.30)
        var totalAddition = 0.0

        for (i in quizScores.indices) {
            val score = quizScores[i].coerceIn(0, 3)
            val weight = weights.getOrElse(i) { 1.75 / (quizScores.size * 3) }
            totalAddition += (score.toDouble() / 3.0) * weight
        }

        val level = (1.50 + totalAddition).coerceIn(1.50, 3.25)
        return Pair(roundToTwoDecimals(level), INITIAL_CONFIDENCE)
    }

    /**
     * Calculates rating updates for all 4 players in a doubles match.
     *
     * @param team1Player1 Player 1 of Team 1.
     * @param team1Player2 Player 2 of Team 1.
     * @param team2Player1 Player 1 of Team 2.
     * @param team2Player2 Player 2 of Team 2.
     * @param team1GamesWon Total games won by Team 1 across the match.
     * @param team2GamesWon Total games won by Team 2 across the match.
     * @param winningTeam Winning team number: 1 for Team 1, 2 for Team 2.
     * @return Map of userId to [RatingResult].
     */
    fun calculateMatchRating(
        team1Player1: PlayerRating,
        team1Player2: PlayerRating,
        team2Player1: PlayerRating,
        team2Player2: PlayerRating,
        team1GamesWon: Int,
        team2GamesWon: Int,
        winningTeam: Int
    ): Map<String, RatingResult> {
        require(winningTeam in 1..2) { "winningTeam must be 1 or 2 (received $winningTeam)" }
        require(team1GamesWon >= 0 && team2GamesWon >= 0) { "Games won must be non-negative" }

        // 1. Team mean ratings
        val team1Mean = (team1Player1.level + team1Player2.level) / 2.0
        val team2Mean = (team2Player1.level + team2Player2.level) / 2.0

        // 2. Win expectancies using logistic formula
        val exponentA = (team2Mean - team1Mean) / SCALE_FACTOR
        val expectedA = 1.0 / (1.0 + 10.0.pow(exponentA))
        val expectedB = 1.0 - expectedA

        // 3. Match margin multiplier
        val totalGames = team1GamesWon + team2GamesWon
        val rho = if (totalGames > 0) {
            kotlin.math.abs(team1GamesWon - team2GamesWon).toDouble() / totalGames.toDouble()
        } else {
            0.0
        }
        val marginMultiplier = 0.75 + 0.60 * sqrt(rho)

        // 4. Base team deltas
        val actualA = if (winningTeam == 1) 1.0 else 0.0
        val actualB = 1.0 - actualA
        val teamDeltaA = K_FACTOR * (actualA - expectedA) * marginMultiplier
        val teamDeltaB = K_FACTOR * (actualB - expectedB) * marginMultiplier

        // 5. Individual asymmetric calculations
        val results = mutableMapOf<String, RatingResult>()

        fun computePlayerResult(player: PlayerRating, teamMean: Double, teamDelta: Double): RatingResult {
            val safeMean = if (teamMean <= 0.0) 1.0 else teamMean
            val asymmetricFactor = 1.0 + 0.25 * ((safeMean - player.level) / safeMean)
            val confidenceFactor = 1.30 - 0.50 * player.confidence.coerceIn(0.10, 1.00)
            val individualDelta = teamDelta * asymmetricFactor * confidenceFactor

            val unroundedNewLevel = (player.level + individualDelta).coerceIn(MIN_RATING, MAX_RATING)
            val newLevel = roundToTwoDecimals(unroundedNewLevel)
            val effectiveDelta = roundToTwoDecimals(newLevel - player.level)
            val newConfidence = roundToTwoDecimals(minOf(1.00, player.confidence + CONFIDENCE_STEP))

            return RatingResult(
                userId = player.userId,
                oldLevel = player.level,
                newLevel = newLevel,
                delta = effectiveDelta,
                oldConfidence = player.confidence,
                newConfidence = newConfidence
            )
        }

        results[team1Player1.userId] = computePlayerResult(team1Player1, team1Mean, teamDeltaA)
        results[team1Player2.userId] = computePlayerResult(team1Player2, team1Mean, teamDeltaA)
        results[team2Player1.userId] = computePlayerResult(team2Player1, team2Mean, teamDeltaB)
        results[team2Player2.userId] = computePlayerResult(team2Player2, team2Mean, teamDeltaB)

        return results
    }

    /**
     * Overload accepting a [Match] model and map of [User] domain models.
     */
    fun calculateMatchRating(
        match: Match,
        users: Map<String, User>
    ): Map<String, RatingResult> {
        val t1p1User = users[match.team1Player1Id]
            ?: throw IllegalArgumentException("User ${match.team1Player1Id} not found")
        val t1p2Id = match.team1Player2Id
            ?: throw IllegalArgumentException("Doubles match requires team1Player2Id")
        val t1p2User = users[t1p2Id]
            ?: throw IllegalArgumentException("User $t1p2Id not found")

        val t2p1User = users[match.team2Player1Id]
            ?: throw IllegalArgumentException("User ${match.team2Player1Id} not found")
        val t2p2Id = match.team2Player2Id
            ?: throw IllegalArgumentException("Doubles match requires team2Player2Id")
        val t2p2User = users[t2p2Id]
            ?: throw IllegalArgumentException("User $t2p2Id not found")

        val team1Games = match.set1T1 + match.set2T1 + (match.set3T1 ?: 0)
        val team2Games = match.set1T2 + match.set2T2 + (match.set3T2 ?: 0)

        return calculateMatchRating(
            team1Player1 = PlayerRating(t1p1User.id, t1p1User.level, t1p1User.levelConfidence),
            team1Player2 = PlayerRating(t1p2User.id, t1p2User.level, t1p2User.levelConfidence),
            team2Player1 = PlayerRating(t2p1User.id, t2p1User.level, t2p1User.levelConfidence),
            team2Player2 = PlayerRating(t2p2User.id, t2p2User.level, t2p2User.levelConfidence),
            team1GamesWon = team1Games,
            team2GamesWon = team2Games,
            winningTeam = match.winningTeam
        )
    }

    private fun roundToTwoDecimals(value: Double): Double {
        return kotlin.math.round(value * 100.0) / 100.0
    }
}
