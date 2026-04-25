package org.niikage.planr.features.events.repository

import org.niikage.planr.features.events.domain.EventDomain
import org.niikage.planr.features.events.domain.EventId
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.shared.kernel.PageRequest

interface EventRepository {
    suspend fun findById(id: EventId): EventDomain?

    suspend fun findAllByCreatorId(creatorId: UserId, pageRequest: PageRequest): List<EventDomain>

    suspend fun createEvent(event: EventDomain): EventDomain
    suspend fun updateEvent(event: EventDomain): EventDomain
    suspend fun deleteById(id: EventId)
}