package org.niikage.planr.features.eventparticipants.service

import org.niikage.planr.features.eventparticipants.query.EventParticipantRole
import org.niikage.planr.features.eventparticipants.query.EventParticipantsList
import org.niikage.planr.features.events.domain.EventId
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.shared.kernel.PageRequest

interface EventParticipantService {
    suspend fun getEventParticipants(
        eventId: EventId,
        pageRequest: PageRequest
    ): EventParticipantsList

    suspend fun addEventCreator(
        eventId: EventId,
        userId: UserId
    )

    suspend fun inviteParticipants(
        eventId: EventId,
        userIds: List<UserId>
    ): List<UserId>

    suspend fun addParticipant(
        eventId: EventId,
        userId: UserId
    ): UserId

    suspend fun addParticipants(
        eventId: EventId,
        userIds: List<UserId>
    ): List<UserId>

    suspend fun removeParticipant(
        eventId: EventId,
        userId: UserId
    )

    suspend fun removeParticipants(
        eventId: EventId,
        userIds: List<UserId>
    ): Int
}