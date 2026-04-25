package org.niikage.planr.features.eventparticipants.service.impl

import org.niikage.planr.configuration.rabbitmq.RabbitConstants
import org.niikage.planr.features.eventparticipants.query.EventParticipantRole
import org.niikage.planr.features.eventparticipants.query.EventParticipantsList
import org.niikage.planr.features.eventparticipants.repository.EventParticipantRepository
import org.niikage.planr.features.eventparticipants.service.EventParticipantService
import org.niikage.planr.features.events.domain.EventId
import org.niikage.planr.features.events.query.EventQueryRepository
import org.niikage.planr.features.invitations.domain.EventInvitationTarget
import org.niikage.planr.features.invitations.domain.NamedInvitation
import org.niikage.planr.features.invitations.service.InvitationService
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.features.users.domain.toUserId
import org.niikage.planr.features.users.query.UserQueryRepository
import org.niikage.planr.features.users.service.UserService
import org.niikage.planr.shared.kernel.PageRequest
import org.springframework.stereotype.Service

@Service
class EventParticipantServiceImpl(
    private val repo: EventParticipantRepository,
    private val eventQueryRepo: EventQueryRepository,
    private val userQueryRepo: UserQueryRepository,
    private val invitationService: InvitationService,
) : EventParticipantService {
    // ==================== GET ====================
    override suspend fun getEventParticipants(
        eventId: EventId,
        pageRequest: PageRequest
    ): EventParticipantsList {
        return repo.findAllByEventId(eventId, pageRequest)
    }

    // ==================== ADD ====================
    override suspend fun addEventCreator(
        eventId: EventId,
        userId: UserId
    ) {
        repo.addParticipant(eventId, userId, EventParticipantRole.CREATOR)
    }

    override suspend fun inviteParticipants(
        eventId: EventId,
        userIds: List<UserId>
    ): List<UserId> {
        val event = eventQueryRepo.findById(eventId.value)
        val users = userQueryRepo.findAllByIds(userIds.map { it.value })

        val invitations = users.map {
            NamedInvitation(
                target = EventInvitationTarget(
                    event = event
                ),
                sender = event.creator!!,
                receiver = it.value,
                routingKey = RabbitConstants.buildKey(it.value, "event.invitation")
            )
        }

        invitationService.sendInvitations(invitations)
        return users.keys.map { it.toUserId() }
    }

    override suspend fun addParticipant(
        eventId: EventId,
        userId: UserId
    ): UserId {
        return repo.addParticipant(eventId, userId)
    }

    override suspend fun addParticipants(
        eventId: EventId,
        userIds: List<UserId>
    ): List<UserId> {
        return repo.addParticipants(eventId, userIds)
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
        userIds: List<UserId>
    ): Int {
        return repo.deleteParticipants(eventId, userIds)
    }
}