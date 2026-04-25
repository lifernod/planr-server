package org.niikage.planr.features.eventparticipants.service

import org.niikage.planr.features.eventparticipants.query.EventParticipantsList
import org.niikage.planr.features.events.domain.EventId
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.shared.kernel.PageRequest

interface EventParticipantService {
    suspend fun getEventParticipants(
        eventId: EventId,
        pageRequest: PageRequest
    ): EventParticipantsList

    suspend fun addParticipant(
        eventId: EventId,
        userId: UserId
    ): UserId

    suspend fun addParticipants(
        eventId: EventId,
        users: List<UserId>
    ): List<UserId>

    suspend fun removeParticipant(
        eventId: EventId,
        userId: UserId
    )

    suspend fun removeParticipants(
        eventId: EventId,
        users: List<UserId>
    ): Int
}