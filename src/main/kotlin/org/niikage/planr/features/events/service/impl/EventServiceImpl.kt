package org.niikage.planr.features.events.service.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.niikage.planr.features.eventparticipants.service.EventParticipantService
import org.niikage.planr.features.events.domain.EventDomain
import org.niikage.planr.features.events.domain.EventId
import org.niikage.planr.features.events.dto.EventCreateRequest
import org.niikage.planr.features.events.dto.EventUpdateRequest
import org.niikage.planr.features.events.query.EventQueryRepository
import org.niikage.planr.features.events.repository.EventRepository
import org.niikage.planr.features.events.service.EventService
import org.niikage.planr.features.invitations.domain.EventInvitationTarget
import org.niikage.planr.features.invitations.domain.UnnamedInvitation
import org.niikage.planr.features.invitations.service.InvitationService
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.shared.exceptions.UnauthorizedException
import org.niikage.planr.shared.exceptions.maybeNotFound
import org.niikage.planr.shared.exceptions.maybeViolation
import org.niikage.planr.shared.kernel.PageRequest
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class EventServiceImpl(
    private val repo: EventRepository,
    private val queryRepo: EventQueryRepository,
    private val participantService: EventParticipantService,
    private val applicationScore: CoroutineScope,
    private val invitationService: InvitationService
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

    override suspend fun getParticipatedEvents(
        userId: UserId,
        pageRequest: PageRequest
    ): List<EventDomain> {
        return repo.findAllByUserId(userId, pageRequest)
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

        val createdEvent = maybeNotFound("Создатель события не найден") {
            repo.createEvent(event)
        }

        applicationScore.launch {
            val event = queryRepo.findById(createdEvent.id.value)

            val unnamedInvitation = UnnamedInvitation(
                invitationId = event.id,
                sender = event.creator!!,
                target = EventInvitationTarget(
                    event = event
                )
            )
            invitationService.createUnnamedInvitation(unnamedInvitation)

            participantService.addEventCreator(createdEvent.id, createdEvent.creatorId)
        }

        return createdEvent
    }

    // ==================== UPDATE ====================
    override suspend fun update(
        id: EventId,
        requestFromUser: UserId,
        request: EventUpdateRequest
    ): EventDomain {
        val event = getEvent(id)
        if (event.creatorId != requestFromUser)
            throw UnauthorizedException("Редактировать событие может только его создатель")

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