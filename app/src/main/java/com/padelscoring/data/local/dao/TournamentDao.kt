package com.padelscoring.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.padelscoring.data.local.entity.TournamentEntity
import com.padelscoring.data.local.entity.TournamentMatchEntity
import com.padelscoring.data.local.entity.TournamentParticipantEntity
import com.padelscoring.data.local.entity.TournamentRoundEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for tournaments, participants, rounds, and matches.
 */
@Dao
interface TournamentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: TournamentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipants(participants: List<TournamentParticipantEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRounds(rounds: List<TournamentRoundEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<TournamentMatchEntity>)

    @Transaction
    suspend fun insertCompleteTournament(
        tournament: TournamentEntity,
        participants: List<TournamentParticipantEntity>,
        rounds: List<TournamentRoundEntity>,
        matches: List<TournamentMatchEntity>
    ) {
        insertTournament(tournament)
        insertParticipants(participants)
        insertRounds(rounds)
        insertMatches(matches)
    }

    @Query("SELECT * FROM tournaments WHERE id = :id")
    fun getTournamentFlow(id: String): Flow<TournamentEntity?>

    @Query("SELECT * FROM tournaments WHERE id = :id")
    suspend fun getTournamentById(id: String): TournamentEntity?

    @Query("SELECT * FROM tournaments ORDER BY createdAt DESC")
    fun getAllTournaments(): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournament_participants WHERE tournamentId = :tournamentId ORDER BY rank ASC")
    fun getParticipantsFlow(tournamentId: String): Flow<List<TournamentParticipantEntity>>

    @Query("SELECT * FROM tournament_participants WHERE tournamentId = :tournamentId ORDER BY rank ASC")
    suspend fun getParticipants(tournamentId: String): List<TournamentParticipantEntity>

    @Query("SELECT * FROM tournament_rounds WHERE tournamentId = :tournamentId ORDER BY roundNumber ASC")
    fun getRoundsFlow(tournamentId: String): Flow<List<TournamentRoundEntity>>

    @Query("SELECT * FROM tournament_rounds WHERE tournamentId = :tournamentId ORDER BY roundNumber ASC")
    suspend fun getRounds(tournamentId: String): List<TournamentRoundEntity>

    @Query("SELECT * FROM tournament_rounds WHERE id = :id")
    suspend fun getRoundById(id: String): TournamentRoundEntity?

    @Query("UPDATE tournament_rounds SET status = :status WHERE id = :roundId")
    suspend fun updateRoundStatus(roundId: String, status: String)

    @Query("SELECT * FROM tournament_matches WHERE roundId = :roundId ORDER BY courtNumber ASC")
    fun getRoundMatchesFlow(roundId: String): Flow<List<TournamentMatchEntity>>

    @Query("SELECT * FROM tournament_matches WHERE roundId = :roundId ORDER BY courtNumber ASC")
    suspend fun getRoundMatches(roundId: String): List<TournamentMatchEntity>

    @Query("SELECT * FROM tournament_matches WHERE tournamentId = :tournamentId ORDER BY courtNumber ASC")
    fun getTournamentMatchesFlow(tournamentId: String): Flow<List<TournamentMatchEntity>>

    @Query("SELECT * FROM tournament_matches WHERE tournamentId = :tournamentId ORDER BY courtNumber ASC")
    suspend fun getTournamentMatches(tournamentId: String): List<TournamentMatchEntity>

    @Query("SELECT * FROM tournament_matches WHERE id = :id")
    suspend fun getMatchById(id: String): TournamentMatchEntity?

    @Transaction
    @Query("UPDATE tournament_matches SET team1Score = :t1, team2Score = :t2, isCompleted = 1 WHERE id = :matchId")
    suspend fun updateMatchScore(matchId: String, t1: Int, t2: Int)

    @Query("UPDATE tournaments SET status = :status WHERE id = :tournamentId")
    suspend fun updateTournamentStatus(tournamentId: String, status: String)

    @Update
    suspend fun updateTournament(tournament: TournamentEntity)

    @Delete
    suspend fun deleteTournament(tournament: TournamentEntity)

    @Query("DELETE FROM tournaments WHERE id = :id")
    suspend fun deleteTournamentById(id: String)
}
