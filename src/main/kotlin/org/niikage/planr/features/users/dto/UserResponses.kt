package org.niikage.planr.features.users.dto

import org.niikage.planr.features.users.domain.UserDomain
import org.niikage.planr.features.users.domain.UserRole
import java.time.OffsetDateTime
import java.util.*

data class UserSocialsResponse(
    val tgId: String? = null,
    val vkId: String? = null
)

data class UserResponse(
    val id: UUID,
    val name: String,
    val role: UserRole,
    val socials: UserSocialsResponse,
    val createdAt: OffsetDateTime
)

fun UserDomain.toResponse(): UserResponse {
    return UserResponse(
        id = this.id.value,
        name = this.name,
        role = this.role,
        socials = UserSocialsResponse(
            tgId = this.socials.tgId,
            vkId = this.socials.vkId
        ),
        createdAt = this.createdAt
    )
}