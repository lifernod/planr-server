package org.niikage.planr.features.authentication

import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.features.users.domain.UserRole


data class SecurityUserClaims(
    val id: UserId,
    val role: UserRole,
    val tgId: String? = null,
    val vkId: String? = null
)
