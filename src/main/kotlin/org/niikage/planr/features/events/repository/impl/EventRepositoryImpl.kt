package org.niikage.planr.features.events.repository.impl

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.niikage.planr.features.events.domain.EventDomain
import org.niikage.planr.features.events.domain.EventId
import org.niikage.planr.features.events.domain.toDomain
import org.niikage.planr.features.events.domain.toEntity
import org.niikage.planr.features.events.dto.EventCreateRequest
import org.niikage.planr.features.events.dto.EventUpdateRequest
import org.niikage.planr.features.events.repository.EventRepository
import org.niikage.planr.features.events.repository.data.EventDataRepository
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.shared.kernel.PageRequest
import org.springframework.stereotype.Repository

@Repository
class EventRepositoryImpl(
    private val repo: EventDataRepository
) : EventRepository {
    // ==================== GET ====================
    override suspend fun findById(id: EventId): EventDomain? {
        return repo.findById(id.value)?.toDomain()
    }

    override suspend fun findAllByCreatorId(
        creatorId: UserId,
        pageRequest: PageRequest
    ): List<EventDomain> {
        return repo.findAllByCreatorId(
            creatorId.value,
            limit = pageRequest.limit,
            offset = pageRequest.offset
        ).map { it.toDomain() }.toList()
    }

    // ==================== CREATE ====================
    override suspend fun createEvent(event: EventDomain): EventDomain {
        return repo.save(event.toEntity()).toDomain()
    }

    // ==================== UPDATE ====================
    override suspend fun updateEvent(event: EventDomain): EventDomain {
        return repo.save(event.toEntity()).toDomain()
    }

    // ==================== DELETE ====================
    override suspend fun deleteById(id: EventId) {
        repo.deleteById(id.value)
    }
}