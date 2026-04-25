package org.niikage.planr.features.events.service.impl

import org.niikage.planr.features.eventparticipants.query.EventParticipantQueryRepository
import org.niikage.planr.features.eventparticipants.query.entity.EventParticipantRole
import org.niikage.planr.features.events.domain.EventDomain
import org.niikage.planr.features.events.domain.EventId
import org.niikage.planr.features.events.dto.EventCreateRequest
import org.niikage.planr.features.events.dto.EventUpdateRequest
import org.niikage.planr.features.events.repository.EventRepository
import org.niikage.planr.features.events.service.EventService
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.shared.exceptions.maybeNotFound
import org.niikage.planr.shared.exceptions.maybeViolation
import org.niikage.planr.shared.kernel.PageRequest
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class EventServiceImpl(
    private val repo: EventRepository,
    private val participantRepository: EventParticipantQueryRepository
) : EventService {
    // ==================== GET ====================
    override suspend fun getEvent(id: EventId): EventDomain {
        return maybeNotFound("Событие не найдено") {
            repo.findById(id)
        }
    }

    override suspend fun getCreatedEvents(
        creatorId: UserId,
        pageRequest: PageRequest
    ): List<EventDomain> {
        return maybeNotFound("Создатель события не найден") {
            repo.findAllByCreatorId(creatorId, pageRequest)
        }
    }

    // ==================== CREATE ====================
    override suspend fun create(
        creatorId: UserId,
        request: EventCreateRequest
    ): EventDomain {
        val event = EventDomain(
            id = EventId.random(),
            creatorId = creatorId,
            title = request.title,
            description = request.description,
            location = request.location,
            type = request.type,
            startTime = request.startTime,
            endTime = request.endTime,
            createdAt = OffsetDateTime.now()
        )

        val createdEvent = maybeViolation("Создатель событие не найден") {
            repo.createEvent(event)
        }

        participantRepository.addParticipant(createdEvent.id.value, creatorId.value, EventParticipantRole.CREATOR)
        return createdEvent
    }

    // ==================== UPDATE ====================
    override suspend fun update(
        id: EventId,
        request: EventUpdateRequest
    ): EventDomain {
        val event = getEvent(id)

        val updatedEvent = event.copy(
            title = request.title ?: event.title,
            description = request.description ?: event.description,
            location = request.location ?: event.location,
            startTime = request.startTime ?: event.startTime,
            endTime = request.endTime ?: event.endTime
        )
        updatedEvent.validate()

        return repo.updateEvent(updatedEvent)
    }

    // ==================== DELETE ====================
    override suspend fun delete(id: EventId) {
        repo.deleteById(id)
    }
}