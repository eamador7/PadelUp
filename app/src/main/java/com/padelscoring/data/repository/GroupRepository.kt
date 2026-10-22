package com.padelscoring.data.repository

import com.padelscoring.data.local.dao.GroupDao
import com.padelscoring.data.local.dao.UserDao
import com.padelscoring.data.local.mapper.toDomain
import com.padelscoring.data.local.mapper.toEntity
import com.padelscoring.domain.model.Group
import com.padelscoring.domain.model.GroupMember
import com.padelscoring.ui.screens.GroupMemberRanking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Repository interface for social groups and club squad leaderboards.
 */
interface GroupRepository {
    fun observeGroupLeaderboard(groupId: String): Flow<List<GroupMemberRanking>>
    fun observeAllGroups(): Flow<List<Group>>
    fun observeGroup(groupId: String): Flow<Group?>
    suspend fun getGroupById(groupId: String): Group?
    suspend fun createGroup(group: Group)
    suspend fun addMember(groupMember: GroupMember)
}

/**
 * Default Room-backed implementation of [GroupRepository].
 */
class GroupRepositoryImpl(
    private val groupDao: GroupDao,
    private val userDao: UserDao
) : GroupRepository {

    override fun observeGroupLeaderboard(groupId: String): Flow<List<GroupMemberRanking>> {
        return combine(
            groupDao.getGroupLeaderboard(groupId),
            userDao.getAllUsers()
        ) { memberEntities, userEntities ->
            val userMap = userEntities.associate { it.id to it.name }
            memberEntities.mapIndexed { index, member ->
                val userName = userMap[member.userId] ?: "Member ${member.userId.take(6)}"
                val winRate = 60 // Baseline win rate or calculated
                GroupMemberRanking(
                    rank = index + 1,
                    name = userName,
                    elo = member.groupElo.toInt(),
                    trophiesWon = member.tournamentsWon,
                    winRate = winRate
                )
            }
        }
    }

    override fun observeAllGroups(): Flow<List<Group>> {
        return groupDao.getAllGroups().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeGroup(groupId: String): Flow<Group?> {
        return groupDao.getGroupFlow(groupId).map { it?.toDomain() }
    }

    override suspend fun getGroupById(groupId: String): Group? {
        return groupDao.getGroupById(groupId)?.toDomain()
    }

    override suspend fun createGroup(group: Group) {
        groupDao.insertGroup(group.toEntity())
    }

    override suspend fun addMember(groupMember: GroupMember) {
        groupDao.insertMember(groupMember.toEntity())
    }
}
