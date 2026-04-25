package org.niikage.planr.features.invitations.domain

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.niikage.planr.features.events.query.EventView

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "__type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = EventInvitationTarget::class, name = "event")
)
sealed interface InvitationTarget

data class EventInvitationTarget(
    val event: EventView
) : InvitationTarget