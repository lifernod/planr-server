package org.niikage.planr.features.users.repository

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.niikage.planr.features.users.domain.UserDomain
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.features.users.domain.toDomain
import org.niikage.planr.features.users.domain.toEntity
import org.niikage.planr.features.users.repository.data.UserDataRepository
import org.springframework.stereotype.Repository

@Repository
class UserRepository(
    private val repo: UserDataRepository
){
    // ==================== GET ====================
    suspend fun findById(id: UserId): UserDomain? {
        return repo.findById(id.value)?.toDomain()
    }

    suspend fun findByTgId(tgId: String): UserDomain? {
        return repo.findByTgId(tgId)?.toDomain()
    }

    suspend fun findByVkId(vkId: String): UserDomain? {
        return repo.findByVkId(vkId)?.toDomain()
    }

    suspend fun findAllByIds(ids: List<UserId>): List<UserDomain> {
        return repo
            .findAllById(ids.map { it.value })
            .map { it.toDomain() }
            .toList()
    }

    // ==================== CREATE ====================
    suspend fun createUser(user: UserDomain): UserDomain {
        return repo.save(user.toEntity()).toDomain()
    }

    // ==================== UPDATE ====================
    suspend fun updateUser(user: UserDomain): UserDomain {
        return repo.save(user.toEntity()).toDomain()
    }

    // ==================== DELETE ====================
    suspend fun deleteById(id: UserId) {
        repo.deleteById(id.value)
    }
}