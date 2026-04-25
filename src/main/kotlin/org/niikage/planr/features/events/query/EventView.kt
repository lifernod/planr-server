package org.niikage.planr.features.events.query

import io.r2dbc.spi.Row
import org.niikage.planr.features.events.domain.EventType
import org.niikage.planr.features.users.query.UserView
import org.niikage.planr.shared.utils.optional
import org.niikage.planr.shared.utils.required
import java.time.OffsetDateTime
import java.util.UUID

data class EventView(
    val id: UUID,
    val title: String,
    val type: EventType,
    val description: String? = null,
    val location: String,
    val startTime: OffsetDateTime,
    val endTime: OffsetDateTime? = null,

    val creatorId: UUID,
    var creator: UserView? = null
)

fun Row.mapEvent(
    prefix: String = "event",
): EventView {
    return EventView(
        id = this.required("${prefix}_id"),
        title = this.required("${prefix}_title"),
        type = EventType.valueOf(this.required("${prefix}_type")),
        description = this.optional("${prefix}_description"),
        location = this.required("${prefix}_location"),
        startTime = this.required("${prefix}_start_time"),
        endTime = this.optional("${prefix}_end_time"),
        creatorId = this.required("${prefix}_creator_id")
    )
}