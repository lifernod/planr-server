package org.niikage.planr.features.events.dto

import org.niikage.planr.features.events.domain.EventDomain
import org.niikage.planr.features.events.domain.EventType
import java.time.OffsetDateTime
import java.util.*

data class EventResponse(
    val id: UUID,
    val creatorId: UUID,
    val title: String,
    val type: EventType,
    val description: String? = null,
    val location: String,
    val startTime: OffsetDateTime,
    val endTime: OffsetDateTime? = null,
    val createdAt: OffsetDateTime,
)

fun EventDomain.toResponse(): EventResponse =
    EventResponse(
        id = id.value,
        creatorId = creatorId.value,
        title = title,
        type = type,
        description = description,
        location = location,
        startTime = startTime,
        endTime = endTime,
        createdAt = createdAt
    )