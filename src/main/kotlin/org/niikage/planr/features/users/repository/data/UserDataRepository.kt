package org.niikage.planr.features.users.repository.data

import org.niikage.planr.features.users.entity.UserEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface UserDataRepository : CoroutineCrudRepository<UserEntity, UUID> {
    suspend fun findByTgId(id: String): UserEntity?
    suspend fun findByVkId(id: String): UserEntity?
}