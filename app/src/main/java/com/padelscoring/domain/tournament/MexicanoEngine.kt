package com.padelscoring.domain.tournament

import com.padelscoring.domain.model.TournamentMatch
import com.padelscoring.domain.model.TournamentParticipant
import com.padelscoring.domain.model.TournamentRound
import java.util.UUID

/**
 * Pure Kotlin dynamic scheduling engine for Mexicano padel tournaments.
 *
 * Implements:
 * 1. Tiered court ranking: top 4 players on Court 1, ranks 5–8 on Court 2, etc.
 * 2. Parity balance pairing: Rank 1 + Rank 4 vs Rank 2 + Rank 3.
 * 3. History Guard: If Rank 1 and 4 have already partnered in a past round,
 *    automatically swaps to Rank 1 + 3 vs Rank 2 + 4 to avoid partner collisions.
 */
object MexicanoEngine {

    /**
     * Generates the next round of a Mexicano tournament based on current standings and past history.
     *
     * @param tournamentId Identifier of the tournament.
     * @param roundNumber Next round number to generate (e.g. 1, 2, 3...).
     * @param rankedParticipants List of participants, typically computed via [AmericanoEngine.calculateLeaderboard].
     * @param pastRounds Previously scheduled or played rounds to inspect for partnership history.
     * @param totalCourts Available courts.
     * @return Generated [TournamentRound] with dynamic pairings.
     */
    fun generateNextRound(
        tournamentId: String,
        roundNumber: Int,
        rankedParticipants: List<TournamentParticipant>,
        pastRounds: List<TournamentRound>,
        totalCourts: Int
    ): TournamentRound {
        require(rankedParticipants.size >= 4) {
            "Mexicano requires at least 4 participants (received ${rankedParticipants.size})"
        }
        require(totalCourts >= 1) {
            "Total courts must be at least 1 (received $totalCourts)"
        }
        require(roundNumber >= 1) {
            "Round number must be at least 1 (received $roundNumber)"
        }

        val sortedParticipants = rankedParticipants.sortedWith(
            compareBy<TournamentParticipant> { it.rank }
                .thenByDescending { it.totalPoints }
                .thenByDescending { it.totalDiff }
                .thenBy { it.seed }
                .thenBy { it.playerName }
        )

        val pastPartnershipCounts = mutableMapOf<Pair<String, String>, Int>()
        for (round in pastRounds) {
            for (match in round.matches) {
                val pair1 = canonicalPair(match.team1Player1Id, match.team1Player2Id)
                val pair2 = canonicalPair(match.team2Player1Id, match.team2Player2Id)
                pastPartnershipCounts[pair1] = (pastPartnershipCounts[pair1] ?: 0) + 1
                pastPartnershipCounts[pair2] = (pastPartnershipCounts[pair2] ?: 0) + 1
            }
        }

        val courtsToUse = minOf(totalCourts, sortedParticipants.size / 4)
        val roundId = UUID.randomUUID().toString()
        val matches = mutableListOf<TournamentMatch>()

        for (courtIndex in 0 until courtsToUse) {
            val quartet = sortedParticipants.subList(courtIndex * 4, (courtIndex + 1) * 4)
            val p1 = quartet[0]
            val p2 = quartet[1]
            val p3 = quartet[2]
            val p4 = quartet[3]

            data class PairingOption(
                val team1: Pair<TournamentParticipant, TournamentParticipant>,
                val team2: Pair<TournamentParticipant, TournamentParticipant>
            )

            // Three possible permutations within the quartet:
            // Option 1 (Standard Parity): 1+4 vs 2+3
            // Option 2 (Swap 1):          1+3 vs 2+4
            // Option 3 (Swap 2):          1+2 vs 3+4
            val options = listOf(
                PairingOption(p1 to p4, p2 to p3),
                PairingOption(p1 to p3, p2 to p4),
                PairingOption(p1 to p2, p3 to p4)
            )

            fun collisionCount(option: PairingOption): Int {
                val c1 = pastPartnershipCounts[canonicalPair(option.team1.first.playerId, option.team1.second.playerId)] ?: 0
                val c2 = pastPartnershipCounts[canonicalPair(option.team2.first.playerId, option.team2.second.playerId)] ?: 0
                return c1 + c2
            }

            // Select the first option with 0 collisions, or the option with the minimal collision count
            val chosenOption = options.firstOrNull { collisionCount(it) == 0 }
                ?: options.minByOrNull { collisionCount(it) }
                ?: options[0]

            matches.add(
                TournamentMatch(
                    id = UUID.randomUUID().toString(),
                    roundId = roundId,
                    tournamentId = tournamentId,
                    courtNumber = courtIndex + 1,
                    team1Player1Id = chosenOption.team1.first.playerId,
                    team1Player2Id = chosenOption.team1.second.playerId,
                    team2Player1Id = chosenOption.team2.first.playerId,
                    team2Player2Id = chosenOption.team2.second.playerId,
                    team1Score = 0,
                    team2Score = 0,
                    isCompleted = false
                )
            )
        }

        return TournamentRound(
            id = roundId,
            tournamentId = tournamentId,
            roundNumber = roundNumber,
            matches = matches
        )
    }

    private fun canonicalPair(a: String, b: String): Pair<String, String> =
        if (a < b) a to b else b to a
}
