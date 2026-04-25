package org.niikage.planr.features.users.dto

import org.niikage.planr.features.users.domain.UserDomain
import org.niikage.planr.features.users.domain.UserRole
import java.time.OffsetDateTime
import java.util.*

data class UserResponse(
    val id: UUID,
    val name: String,
    val role: UserRole,
    val tgId: String?,
    val vkId: String?,
    val createdAt: OffsetDateTime
)

fun UserDomain.toResponse(): UserResponse {
    return UserResponse(
        id = this.id.value,
        name = this.name,
        role = this.role,
        tgId = this.socials.tgId,
        vkId = this.socials.vkId,
        createdAt = this.createdAt
    )
}