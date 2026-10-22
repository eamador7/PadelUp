package com.padelscoring.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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

/**
 * Main Room SQLite Database for Padel Up with WAL mode, foreign key enforcement,
 * and offline-first local persistence.
 */
@Database(
    entities = [
        UserEntity::class,
        GroupEntity::class,
        GroupMemberEntity::class,
        MatchEntity::class,
        MatchConfirmationEntity::class,
        TournamentEntity::class,
        TournamentParticipantEntity::class,
        TournamentRoundEntity::class,
        TournamentMatchEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PadelDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun matchDao(): MatchDao
    abstract fun tournamentDao(): TournamentDao
    abstract fun groupDao(): GroupDao

    companion object {
        const val DATABASE_NAME = "padel_database.db"

        @Volatile
        private var INSTANCE: PadelDatabase? = null

        private val foreignKeysCallback = object : Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                db.execSQL("PRAGMA foreign_keys = ON;")
            }
        }

        fun getInstance(context: Context): PadelDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        fun buildDatabase(context: Context): PadelDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                PadelDatabase::class.java,
                DATABASE_NAME
            )
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                .addCallback(foreignKeysCallback)
                .build()
        }

        fun buildInMemoryDatabase(context: Context): PadelDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                PadelDatabase::class.java
            )
                .allowMainThreadQueries()
                .addCallback(foreignKeysCallback)
                .build()
        }
    }
}
