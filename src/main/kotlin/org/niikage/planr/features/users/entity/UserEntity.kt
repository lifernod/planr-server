package org.niikage.planr.features.users.entity

import org.niikage.planr.features.users.domain.UserRole
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime
import java.util.*

@Table("users")
data class UserEntity(
    @Id
    val id: UUID,
    val name: String,
    val role: UserRole,
    val tgId: String?,
    val tgUsername: String?,
    val vkId: String?,
    val vkUsername: String?,
    val createdAt: OffsetDateTime,

    @Version
    val version: Long? = null
)
