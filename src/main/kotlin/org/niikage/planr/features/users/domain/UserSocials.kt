package org.niikage.planr.features.users.domain

import org.niikage.planr.features.users.entity.UserEntity

data class UserSocials(
    val tgId: String? = null,
    val tgUsername: String? = null,
    val vkId: String? = null,
    val vkUsername: String? = null
) {
    companion object {
        fun ofTg(tgId: String, tgUsername: String? = null): UserSocials {
            return UserSocials(tgId = tgId, tgUsername = tgUsername)
        }

        fun ofVk(vkId: String, vkUsername: String? = null): UserSocials {
            return UserSocials(vkId = vkId, vkUsername = vkUsername)
        }
    }

    fun isSocialConnected(): Boolean {
        return tgId != null || vkId != null
    }

    fun isTgConnected(): Boolean {
        return tgId != null
    }

    fun isVkConnected(): Boolean {
        return vkId != null
    }
}

fun UserEntity.extractSocials(): UserSocials {
    return UserSocials(
        tgId = this.tgId,
        tgUsername = this.tgUsername,
        vkId = this.vkId,
        vkUsername = this.vkUsername
    )
}