package com.padelscoring.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.padelscoring.data.local.entity.MatchConfirmationEntity
import com.padelscoring.data.local.entity.MatchEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for matches and match confirmations.
 */
@Dao
interface MatchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Query("SELECT * FROM matches ORDER BY playedAt DESC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE id = :id")
    fun getMatchFlow(id: String): Flow<MatchEntity?>

    @Query("SELECT * FROM matches WHERE id = :id")
    suspend fun getMatchById(id: String): MatchEntity?

    @Query("SELECT * FROM matches WHERE confirmationStatus = 'PENDING' ORDER BY playedAt DESC")
    fun getPendingMatches(): Flow<List<MatchEntity>>

    @Query("UPDATE matches SET confirmationStatus = :status WHERE id = :matchId")
    suspend fun updateConfirmationStatus(matchId: String, status: String)

    @Update
    suspend fun updateMatch(match: MatchEntity)

    @Delete
    suspend fun deleteMatch(match: MatchEntity)

    @Query("DELETE FROM matches WHERE id = :id")
    suspend fun deleteMatchById(id: String)

    // Confirmations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfirmation(confirmation: MatchConfirmationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfirmations(confirmations: List<MatchConfirmationEntity>)

    @Query("SELECT * FROM match_confirmations WHERE matchId = :matchId")
    fun getConfirmationsForMatch(matchId: String): Flow<List<MatchConfirmationEntity>>

    @Query("SELECT * FROM match_confirmations WHERE id = :id")
    suspend fun getConfirmationById(id: String): MatchConfirmationEntity?

    @Delete
    suspend fun deleteConfirmation(confirmation: MatchConfirmationEntity)

    @Query("DELETE FROM match_confirmations WHERE id = :id")
    suspend fun deleteConfirmationById(id: String)
}
