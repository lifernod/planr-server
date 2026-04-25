package org.niikage.planr.features.users.repository.impl

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.niikage.planr.features.users.domain.UserDomain
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.features.users.domain.toDomain
import org.niikage.planr.features.users.domain.toEntity
import org.niikage.planr.features.users.repository.UserRepository
import org.niikage.planr.features.users.repository.data.UserDataRepository
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(
    private val repo: UserDataRepository
) : UserRepository {
    // ==================== GET ====================
    override suspend fun findById(id: UserId): UserDomain? {
        return repo.findById(id.value)?.toDomain()
    }

    override suspend fun findByTgId(tgId: String): UserDomain? {
        return repo.findByTgId(tgId)?.toDomain()
    }

    override suspend fun findByVkId(vkId: String): UserDomain? {
        return repo.findByVkId(vkId)?.toDomain()
    }

    override suspend fun findAllByIds(ids: List<UserId>): List<UserDomain> {
        return repo
            .findAllById(ids.map { it.value })
            .map { it.toDomain() }
            .toList()
    }

    // ==================== CREATE ====================
    override suspend fun createUser(user: UserDomain): UserDomain {
        return repo.save(user.toEntity()).toDomain()
    }

    // ==================== UPDATE ====================
    override suspend fun updateUser(user: UserDomain): UserDomain {
        return repo.save(user.toEntity()).toDomain()
    }

    // ==================== DELETE ====================
    override suspend fun deleteById(id: UserId) {
        repo.deleteById(id.value)
    }
}