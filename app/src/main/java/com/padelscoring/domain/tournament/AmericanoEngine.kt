package com.padelscoring.domain.tournament

import com.padelscoring.domain.model.Tournament
import com.padelscoring.domain.model.TournamentMatch
import com.padelscoring.domain.model.TournamentParticipant
import com.padelscoring.domain.model.TournamentRound
import java.util.UUID

/**
 * Pure Kotlin scheduling and scoring engine for Americano padel tournaments.
 *
 * Implements:
 * 1. Whist-style cyclic difference sets for standard counts (4, 8, 12, 16 players)
 *    guaranteeing zero duplicate partnerships across the first N-1 rounds.
 * 2. Constraint Satisfaction Problem (CSP) solver for irregular counts (5, 6, 7, 9, 10...)
 *    ensuring evenly distributed rest/bye slots (Var(Byes) <= 1) and minimal duplicate pairings.
 * 3. Point conservation score updating (Score1 + Score2 == targetPoints).
 * 4. Deterministic leaderboard rank calculation with tie-breakers.
 */
object AmericanoEngine {

    /**
     * Generates a complete Americano tournament schedule.
     *
     * @param playerIds List of unique player identifiers. Minimum 4 players.
     * @param totalCourts Available courts. Minimum 1 court.
     * @param totalRounds Optional round count. Defaults to N-1 (standard) or N (odd irregulars).
     * @param tournamentId Associated tournament identifier.
     * @return List of generated [TournamentRound] objects.
     */
    fun generateSchedule(
        playerIds: List<String>,
        totalCourts: Int,
        totalRounds: Int? = null,
        tournamentId: String = UUID.randomUUID().toString()
    ): List<TournamentRound> {
        val n = playerIds.size
        require(n >= 4) { "Americano tournament requires at least 4 players (received $n)" }
        require(totalCourts >= 1) { "Total courts must be at least 1 (received $totalCourts)" }
        require(playerIds.distinct().size == n) { "Player IDs must be unique" }
        if (totalRounds != null) {
            require(totalRounds > 0) { "Total rounds must be at least 1 (received $totalRounds)" }
        }

        val activeCourts = minOf(totalCourts, n / 4)
        val defaultRounds = if (n in listOf(4, 8, 12, 16) && totalCourts >= n / 4) {
            n - 1
        } else if (n % 2 != 0) {
            n
        } else {
            n - 1
        }
        val roundsCount = totalRounds ?: defaultRounds

        return if (n in listOf(4, 8, 12, 16) && totalCourts >= n / 4) {
            generateStandardWhistSchedule(playerIds, activeCourts, roundsCount, tournamentId)
        } else {
            generateIrregularCspSchedule(playerIds, activeCourts, roundsCount, tournamentId)
        }
    }

    /**
     * Updates the score of a match within a tournament round while enforcing
     * point conservation: t1Score + t2Score == tournament.targetPoints.
     */
    fun updateMatchScore(
        tournament: Tournament,
        round: TournamentRound,
        matchId: String,
        t1Score: Int,
        t2Score: Int
    ): TournamentRound {
        require(t1Score >= 0 && t2Score >= 0) { "Scores must be non-negative" }
        require(t1Score + t2Score == tournament.targetPoints) {
            "Total points ($t1Score + $t2Score = ${t1Score + t2Score}) must equal tournament target points (${tournament.targetPoints})"
        }
        val matchExists = round.matches.any { it.id == matchId }
        require(matchExists) { "Match with id $matchId not found in round ${round.roundNumber}" }

        val updatedMatches = round.matches.map { match ->
            if (match.id == matchId) {
                match.copy(
                    team1Score = t1Score,
                    team2Score = t2Score,
                    isCompleted = true
                )
            } else {
                match
            }
        }
        return round.copy(matches = updatedMatches)
    }

    /**
     * Calculates the tournament leaderboard from participant standings and completed matches.
     *
     * Sorting priority:
     * 1. Total points won (descending)
     * 2. Point differential (pointsScored - pointsConceded, descending)
     * 3. Total matches won (descending)
     * 4. Seed (ascending)
     * 5. Player name (ascending)
     */
    fun calculateLeaderboard(
        participants: List<TournamentParticipant>,
        completedMatches: List<TournamentMatch>
    ): List<TournamentParticipant> {
        val completedOnly = completedMatches.filter { it.isCompleted }

        val updatedParticipants = participants.map { participant ->
            var totalPoints = 0
            var totalConceded = 0
            var matchesPlayed = 0
            var matchesWon = 0

            for (match in completedOnly) {
                val inTeam1 = participant.playerId == match.team1Player1Id || participant.playerId == match.team1Player2Id
                val inTeam2 = participant.playerId == match.team2Player1Id || participant.playerId == match.team2Player2Id

                if (inTeam1) {
                    matchesPlayed++
                    totalPoints += match.team1Score
                    totalConceded += match.team2Score
                    if (match.team1Score > match.team2Score) {
                        matchesWon++
                    }
                } else if (inTeam2) {
                    matchesPlayed++
                    totalPoints += match.team2Score
                    totalConceded += match.team1Score
                    if (match.team2Score > match.team1Score) {
                        matchesWon++
                    }
                }
            }

            participant.copy(
                totalPoints = totalPoints,
                totalDiff = totalPoints - totalConceded,
                matchesPlayed = matchesPlayed,
                matchesWon = matchesWon
            )
        }

        val sorted = updatedParticipants.sortedWith(
            compareByDescending<TournamentParticipant> { it.totalPoints }
                .thenByDescending { it.totalDiff }
                .thenByDescending { it.matchesWon }
                .thenBy { it.seed }
                .thenBy { it.playerName }
        )

        return sorted.mapIndexed { index, participant ->
            participant.copy(rank = index + 1)
        }
    }

    // --- Internal Whist & CSP Algorithms ---

    private data class WhistMatchTemplate(
        val team1: Pair<Int, Int>,
        val team2: Pair<Int, Int>
    )

    private fun generateStandardWhistSchedule(
        playerIds: List<String>,
        activeCourts: Int,
        roundsCount: Int,
        tournamentId: String
    ): List<TournamentRound> {
        val n = playerIds.size
        val modulus = n - 1

        val baseTemplates: List<WhistMatchTemplate> = when (n) {
            4 -> listOf(
                WhistMatchTemplate(3 to 0, 1 to 2)
            )
            8 -> listOf(
                WhistMatchTemplate(7 to 0, 1 to 3),
                WhistMatchTemplate(2 to 6, 4 to 5)
            )
            12 -> listOf(
                WhistMatchTemplate(11 to 8, 1 to 9),
                WhistMatchTemplate(10 to 5, 7 to 0),
                WhistMatchTemplate(2 to 3, 6 to 4)
            )
            16 -> listOf(
                WhistMatchTemplate(15 to 8, 14 to 5),
                WhistMatchTemplate(10 to 0, 1 to 13),
                WhistMatchTemplate(12 to 11, 7 to 3),
                WhistMatchTemplate(9 to 2, 4 to 6)
            )
            else -> throw IllegalArgumentException("Standard Whist requires N in {4, 8, 12, 16}")
        }

        val rounds = mutableListOf<TournamentRound>()

        for (r in 0 until roundsCount) {
            val roundId = UUID.randomUUID().toString()
            val shift = r % modulus

            fun mapPlayer(idx: Int): String {
                return if (idx == modulus) {
                    playerIds[modulus]
                } else {
                    playerIds[(idx + shift) % modulus]
                }
            }

            val matches = baseTemplates.take(activeCourts).mapIndexed { courtIdx, template ->
                TournamentMatch(
                    id = UUID.randomUUID().toString(),
                    roundId = roundId,
                    tournamentId = tournamentId,
                    courtNumber = courtIdx + 1,
                    team1Player1Id = mapPlayer(template.team1.first),
                    team1Player2Id = mapPlayer(template.team1.second),
                    team2Player1Id = mapPlayer(template.team2.first),
                    team2Player2Id = mapPlayer(template.team2.second),
                    team1Score = 0,
                    team2Score = 0,
                    isCompleted = false
                )
            }

            rounds.add(
                TournamentRound(
                    id = roundId,
                    tournamentId = tournamentId,
                    roundNumber = r + 1,
                    matches = matches
                )
            )
        }

        return rounds
    }

    private fun generateIrregularCspSchedule(
        playerIds: List<String>,
        activeCourts: Int,
        roundsCount: Int,
        tournamentId: String
    ): List<TournamentRound> {
        val n = playerIds.size
        val playersPerRound = activeCourts * 4
        val byesPerRound = n - playersPerRound

        val byeCounts = playerIds.associateWith { 0 }.toMutableMap()
        val partnerCounts = mutableMapOf<Pair<String, String>, Int>()
        val opponentCounts = mutableMapOf<Pair<String, String>, Int>()

        val rounds = mutableListOf<TournamentRound>()

        for (r in 1..roundsCount) {
            val roundId = UUID.randomUUID().toString()

            // 1. Select bye players ensuring Var(Byes) <= 1
            val sortedForByes = playerIds.sortedWith(
                compareBy<String> { byeCounts[it] ?: 0 }
                    .thenBy { playerIds.indexOf(it) }
            )
            val byesThisRound = sortedForByes.take(byesPerRound).toSet()
            for (byePlayer in byesThisRound) {
                byeCounts[byePlayer] = (byeCounts[byePlayer] ?: 0) + 1
            }

            val playingPlayers = playerIds.filter { it !in byesThisRound }

            // 2. Solve match partitioning
            val courtMatches = solveOptimalCourtPartitions(
                playingPlayers = playingPlayers,
                courtCount = activeCourts,
                partnerCounts = partnerCounts,
                opponentCounts = opponentCounts
            )

            // 3. Record match pairings in history and build TournamentMatch objects
            val matches = courtMatches.mapIndexed { courtIdx, matchPairing ->
                val (t1, t2) = matchPairing
                val pKey1 = canonicalPair(t1.first, t1.second)
                val pKey2 = canonicalPair(t2.first, t2.second)
                partnerCounts[pKey1] = (partnerCounts[pKey1] ?: 0) + 1
                partnerCounts[pKey2] = (partnerCounts[pKey2] ?: 0) + 1

                for (pA in listOf(t1.first, t1.second)) {
                    for (pB in listOf(t2.first, t2.second)) {
                        val oppKey = canonicalPair(pA, pB)
                        opponentCounts[oppKey] = (opponentCounts[oppKey] ?: 0) + 1
                    }
                }

                TournamentMatch(
                    id = UUID.randomUUID().toString(),
                    roundId = roundId,
                    tournamentId = tournamentId,
                    courtNumber = courtIdx + 1,
                    team1Player1Id = t1.first,
                    team1Player2Id = t1.second,
                    team2Player1Id = t2.first,
                    team2Player2Id = t2.second,
                    team1Score = 0,
                    team2Score = 0,
                    isCompleted = false
                )
            }

            rounds.add(
                TournamentRound(
                    id = roundId,
                    tournamentId = tournamentId,
                    roundNumber = r,
                    matches = matches
                )
            )
        }

        return rounds
    }

    private data class MatchPairing(
        val team1: Pair<String, String>,
        val team2: Pair<String, String>
    )

    private fun solveOptimalCourtPartitions(
        playingPlayers: List<String>,
        courtCount: Int,
        partnerCounts: Map<Pair<String, String>, Int>,
        opponentCounts: Map<Pair<String, String>, Int>
    ): List<MatchPairing> {
        fun evaluateMatch(m: MatchPairing): Long {
            val p1 = partnerCounts[canonicalPair(m.team1.first, m.team1.second)] ?: 0
            val p2 = partnerCounts[canonicalPair(m.team2.first, m.team2.second)] ?: 0
            var cost = (p1 * p1 + p2 * p2).toLong() * 1000L

            for (a in listOf(m.team1.first, m.team1.second)) {
                for (b in listOf(m.team2.first, m.team2.second)) {
                    cost += (opponentCounts[canonicalPair(a, b)] ?: 0) * 10L
                }
            }
            return cost
        }

        // For large player counts (> 16 players), use multi-restart greedy min-conflict
        // to prevent combinatorial DFS state explosion and guarantee sub-20ms execution.
        if (playingPlayers.size > 16) {
            return solveLargeCourtPartitionsGreedy(
                playingPlayers,
                courtCount,
                partnerCounts,
                opponentCounts,
                ::evaluateMatch
            )
        }

        var bestCost = Long.MAX_VALUE
        var bestMatches: List<MatchPairing>? = null

        fun dfs(remaining: List<String>, currentMatches: List<MatchPairing>, currentCost: Long) {
            if (remaining.isEmpty()) {
                if (currentCost < bestCost) {
                    bestCost = currentCost
                    bestMatches = currentMatches
                }
                return
            }
            if (currentCost >= bestCost) return

            val p0 = remaining[0]
            val rest = remaining.drop(1)

            data class Candidate(val cost: Long, val trio: List<String>, val match: MatchPairing)
            val candidates = mutableListOf<Candidate>()

            val trios = combinations(rest, 3)
            for (trio in trios) {
                val quartet = listOf(p0) + trio
                val pairings = listOf(
                    MatchPairing(quartet[0] to quartet[1], quartet[2] to quartet[3]),
                    MatchPairing(quartet[0] to quartet[2], quartet[1] to quartet[3]),
                    MatchPairing(quartet[0] to quartet[3], quartet[1] to quartet[2])
                )
                for (m in pairings) {
                    candidates.add(Candidate(evaluateMatch(m), trio, m))
                }
            }

            candidates.sortBy { it.cost }

            // Prune branches to ensure sub-millisecond execution
            val branchLimit = minOf(12, candidates.size)
            for (i in 0 until branchLimit) {
                val candidate = candidates[i]
                if (currentCost + candidate.cost >= bestCost) continue
                val nextRemaining = rest.filter { it !in candidate.trio }
                dfs(nextRemaining, currentMatches + candidate.match, currentCost + candidate.cost)
            }
        }

        dfs(playingPlayers, emptyList(), 0L)
        return bestMatches ?: fallbackPairing(playingPlayers, courtCount)
    }

    private fun solveLargeCourtPartitionsGreedy(
        playingPlayers: List<String>,
        courtCount: Int,
        partnerCounts: Map<Pair<String, String>, Int>,
        opponentCounts: Map<Pair<String, String>, Int>,
        evaluator: (MatchPairing) -> Long
    ): List<MatchPairing> {
        var globalBestMatches: List<MatchPairing>? = null
        var globalBestCost = Long.MAX_VALUE

        val restarts = 25
        val rng = java.util.Random(42)

        for (attempt in 0 until restarts) {
            val unassigned = playingPlayers.toMutableList()
            // Shuffle with pseudo-random seed per attempt for deterministic diversity
            unassigned.shuffle(rng)

            val attemptMatches = mutableListOf<MatchPairing>()
            var attemptCost = 0L

            for (c in 0 until courtCount) {
                if (unassigned.size < 4) break
                val p0 = unassigned.removeAt(0)

                // Pick partner with lowest partnership count
                var bestPartnerIdx = 0
                var minPartnerCost = Int.MAX_VALUE
                for (i in unassigned.indices) {
                    val p = unassigned[i]
                    val cost = partnerCounts[canonicalPair(p0, p)] ?: 0
                    if (cost < minPartnerCost) {
                        minPartnerCost = cost
                        bestPartnerIdx = i
                    }
                }
                val p1 = unassigned.removeAt(bestPartnerIdx)

                // Pick best pair for team 2 from remaining candidates
                var bestPairing: MatchPairing? = null
                var minMatchCost = Long.MAX_VALUE
                var bestOpponentIndices: Pair<Int, Int>? = null

                val sampleLimit = minOf(unassigned.size, 8)
                for (i in 0 until sampleLimit) {
                    for (j in i + 1 until sampleLimit) {
                        val p2 = unassigned[i]
                        val p3 = unassigned[j]
                        val pairing = MatchPairing(p0 to p1, p2 to p3)
                        val cost = evaluator(pairing)
                        if (cost < minMatchCost) {
                            minMatchCost = cost
                            bestPairing = pairing
                            bestOpponentIndices = i to j
                        }
                    }
                }

                if (bestPairing != null && bestOpponentIndices != null) {
                    attemptCost += minMatchCost
                    attemptMatches.add(bestPairing)
                    val (idxA, idxB) = bestOpponentIndices
                    // Remove in descending index order
                    unassigned.removeAt(maxOf(idxA, idxB))
                    unassigned.removeAt(minOf(idxA, idxB))
                } else if (unassigned.size >= 2) {
                    val p2 = unassigned.removeAt(0)
                    val p3 = unassigned.removeAt(0)
                    val fallback = MatchPairing(p0 to p1, p2 to p3)
                    attemptCost += evaluator(fallback)
                    attemptMatches.add(fallback)
                }
            }

            if (attemptMatches.size == courtCount && attemptCost < globalBestCost) {
                globalBestCost = attemptCost
                globalBestMatches = attemptMatches
            }
        }

        return globalBestMatches ?: fallbackPairing(playingPlayers, courtCount)
    }

    private fun fallbackPairing(players: List<String>, courtCount: Int): List<MatchPairing> {
        val matches = mutableListOf<MatchPairing>()
        for (c in 0 until courtCount) {
            val p = players.subList(c * 4, (c + 1) * 4)
            matches.add(MatchPairing(p[0] to p[1], p[2] to p[3]))
        }
        return matches
    }

    private fun canonicalPair(a: String, b: String): Pair<String, String> =
        if (a < b) a to b else b to a

    private fun <T> combinations(list: List<T>, k: Int): List<List<T>> {
        if (k == 0) return listOf(emptyList())
        if (list.size < k) return emptyList()
        val head = list.first()
        val tail = list.drop(1)
        val withHead = combinations(tail, k - 1).map { listOf(head) + it }
        val withoutHead = combinations(tail, k)
        return withHead + withoutHead
    }
}
