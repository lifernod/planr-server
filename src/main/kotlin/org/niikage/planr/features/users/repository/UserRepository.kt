package org.niikage.planr.features.users.repository

import org.niikage.planr.features.users.domain.UserDomain
import org.niikage.planr.features.users.domain.UserId

interface UserRepository {
    suspend fun findById(id: UserId): UserDomain?
    suspend fun findByTgId(tgId: String): UserDomain?
    suspend fun findByVkId(vkId: String): UserDomain?

    suspend fun createUser(user: UserDomain): UserDomain
    suspend fun updateUser(user: UserDomain): UserDomain
    suspend fun deleteById(id: UserId)
}