package org.niikage.planr.features.users.query

import io.r2dbc.spi.Row
import org.niikage.planr.shared.utils.required
import java.time.OffsetDateTime
import java.util.UUID

data class UserView(
    val id: UUID,
    val name: String,
    val tgConnected: Boolean,
    val vkConnected: Boolean,
    val createdAt: OffsetDateTime
)

fun Row.mapUser(prefix: String = "user"): UserView {
    return UserView(
        id = this.required("${prefix}_id"),
        name = this.required("${prefix}_name"),
        tgConnected = this.required("${prefix}_tg_connected"),
        vkConnected = this.required("${prefix}_vk_connected"),
        createdAt = this.required("${prefix}_created_at"),
    )
}
