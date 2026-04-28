package org.niikage.planr.features.users.dto

data class UserSocialsRequest(
    val tgId: String? = null,
    val tgUsername: String? = null,
    val vkId: String? = null,
    val vkUsername: String? = null
)

data class UserCreateRequest(
    val name: String,
    val socials: UserSocialsRequest
)

data class UserUpdateRequest(
    val name: String? = null,
    val socials: UserSocialsRequest? = null,
)