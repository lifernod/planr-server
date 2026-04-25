package org.niikage.planr.features.eventparticipants.query.entity

import io.r2dbc.spi.Row
import org.niikage.planr.features.events.query.EventView
import org.niikage.planr.features.users.query.UserView
import org.niikage.planr.shared.utils.required
import java.util.*

enum class EventParticipantRole {
    PARTICIPANT,
    CREATOR
}

data class EventParticipantView(
    val eventId: UUID,
    var event: EventView? = null,

    val participantId: UUID,
    val role: EventParticipantRole = EventParticipantRole.PARTICIPANT,
    var participant: UserView? = null
)

fun Row.mapEventParticipant(): EventParticipantView {
    return EventParticipantView(
        eventId = this.required("event_id"),
        participantId = this.required("participant_id"),
        role = EventParticipantRole.valueOf(this.required<String>("participant_role"))
    )
}