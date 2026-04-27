package org.niikage.planr.features.events.service

import org.niikage.planr.features.events.domain.EventDomain
import org.niikage.planr.features.events.domain.EventId
import org.niikage.planr.features.events.dto.EventCreateRequest
import org.niikage.planr.features.events.dto.EventUpdateRequest
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.shared.kernel.PageRequest

interface EventService {
    suspend fun getEvent(id: EventId): EventDomain

    suspend fun getCreatedEvents(creatorId: UserId, pageRequest: PageRequest): List<EventDomain>
    suspend fun getParticipatedEvents(userId: UserId, pageRequest: PageRequest): List<EventDomain>

    suspend fun create(creatorId: UserId, request: EventCreateRequest): EventDomain
    suspend fun update(
        id: EventId,
        requestFromUser: UserId,
        request: EventUpdateRequest): EventDomain

    suspend fun delete(id: EventId)
}