package org.niikage.planr.features.events.dto

import org.niikage.planr.features.events.domain.EventType
import java.time.OffsetDateTime

data class EventCreateRequest(
    val title: String,
    val type: EventType,
    val description: String? = null,
    val location: String,
    val startTime: OffsetDateTime,
    val endTime: OffsetDateTime? = null
)

data class EventUpdateRequest(
    val title: String? = null,
    val description: String? = null,
    val location: String? = null,
    val startTime: OffsetDateTime? = null,
    val endTime: OffsetDateTime? = null
)