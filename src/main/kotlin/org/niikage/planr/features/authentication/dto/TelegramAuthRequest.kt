package org.niikage.planr.features.authentication.dto

data class TelegramAuthRequest(
    val initData: String
)

@Suppress("PropertyName")
data class TelegramUserDto(
    val id: Long,
    val first_name: String?,
    val last_name: String?,
    val username: String?,
    val language_code: String?
)

