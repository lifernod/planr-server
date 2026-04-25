package org.niikage.planr.features.eventparticipants.service.impl

import org.niikage.planr.features.eventparticipants.repository.EventParticipantRepository
import org.niikage.planr.features.eventparticipants.query.EventParticipantsList
import org.niikage.planr.features.eventparticipants.service.EventParticipantService
import org.niikage.planr.features.events.domain.EventId
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.shared.kernel.PageRequest
import org.springframework.stereotype.Service

@Service
class EventParticipantServiceImpl(
    private val repo: EventParticipantRepository
) : EventParticipantService {
    // ==================== GET ====================
    override suspend fun getEventParticipants(
        eventId: EventId,
        pageRequest: PageRequest
    ): EventParticipantsList {
        return repo.findAllByEventId(eventId, pageRequest)
    }

    // ==================== ADD ====================
    override suspend fun addParticipant(
        eventId: EventId,
        userId: UserId
    ): UserId {
        return repo.addParticipant(eventId, userId)
    }

    override suspend fun addParticipants(
        eventId: EventId,
        users: List<UserId>
    ): List<UserId> {
        return repo.addParticipants(eventId, users)
    }

    // ==================== REMOVE ====================
    override suspend fun removeParticipant(
        eventId: EventId,
        userId: UserId
    ) {
        repo.deleteParticipant(eventId, userId)
    }

    override suspend fun removeParticipants(
        eventId: EventId,
        users: List<UserId>
    ): Int {
        return repo.deleteParticipants(eventId, users)
    }
}