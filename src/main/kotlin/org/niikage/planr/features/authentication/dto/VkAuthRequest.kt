package org.niikage.planr.features.authentication.dto

data class VkAuthRequest(
    val launchParams: String
)

data class VkUserDto(
    val vkUserId: String,
    val language: String?,
    val platform: String?
)
