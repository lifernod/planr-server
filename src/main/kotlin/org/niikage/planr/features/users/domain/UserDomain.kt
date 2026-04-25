package org.niikage.planr.features.users.domain

import org.niikage.planr.features.users.entity.UserEntity
import java.time.OffsetDateTime

enum class UserRole {
    USER
}

data class UserDomain(
    val id: UserId,
    val name: String,
    val role: UserRole,
    val socials: UserSocials,
    val createdAt: OffsetDateTime,

    internal val version: Long? = null
)

fun UserEntity.toDomain(): UserDomain {
    return UserDomain(
        id = this.id.toUserId(),
        name = this.name,
        role = this.role,
        socials = this.extractSocials(),
        createdAt = this.createdAt,
        version = this.version
    )
}

fun UserDomain.toEntity(): UserEntity {
    return UserEntity(
        id = this.id.value,
        name = this.name,
        role = this.role,
        tgId = this.socials.tgId,
        tgUsername = this.socials.tgUsername,
        vkId = this.socials.vkId,
        vkUsername = this.socials.vkUsername,
        createdAt = this.createdAt,
        version = this.version
    )
}