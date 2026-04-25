package org.niikage.planr.features.users.dto

import org.niikage.planr.features.users.domain.UserSocials

data class UserCreateRequest(
    val name: String,
    val socials: UserSocials
)

data class UserUpdateRequest(
    val name: String? = null,
    val socials: UserSocials? = null,
)