package org.niikage.planr.features.eventparticipants.repository

import org.niikage.planr.features.eventparticipants.query.EventParticipantRole
import org.niikage.planr.features.eventparticipants.query.EventParticipantsList
import org.niikage.planr.features.events.domain.EventId
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.shared.kernel.PageRequest
import java.util.UUID

interface EventParticipantRepository {
    suspend fun findAllByEventId(
        eventId: EventId,
        pageRequest: PageRequest,
        role: EventParticipantRole = EventParticipantRole.PARTICIPANT
    ): EventParticipantsList

    suspend fun addParticipant(
        eventId: EventId,
        userId: UserId,
        role: EventParticipantRole = EventParticipantRole.PARTICIPANT
    ): UserId

    suspend fun addParticipants(
        eventId: EventId,
        userIds: List<UserId>,
    ): List<UserId>

    suspend fun deleteParticipant(
        eventId: EventId,
        userId: UserId
    )

    suspend fun deleteParticipants(
        eventId: EventId,
        userIds: List<UserId>
    ): Int
}