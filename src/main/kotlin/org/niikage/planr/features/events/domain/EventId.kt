package org.niikage.planr.features.events.domain

import java.util.*

@JvmInline
value class EventId(val value: UUID) {
    companion object {
        fun random(): EventId {
            return EventId(UUID.randomUUID())
        }
    }
}

fun UUID.toEventId(): EventId {
    return EventId(this)
}