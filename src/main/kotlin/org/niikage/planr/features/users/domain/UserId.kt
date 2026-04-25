package org.niikage.planr.features.users.domain

import java.security.Principal
import java.util.UUID


@JvmInline
value class UserId(val value: UUID) {
    companion object {
        fun random(): UserId {
            return UserId(UUID.randomUUID())
        }
    }
}

fun UUID.toUserId(): UserId {
    return UserId(this)
}

fun Principal.toUserId(): UserId {
    return UserId(UUID.fromString(this.name))
}