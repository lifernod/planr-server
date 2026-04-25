package org.niikage.planr.features.invitations.service.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.niikage.planr.configuration.rabbitmq.RabbitConstants
import org.niikage.planr.features.invitations.domain.Invitation
import org.niikage.planr.features.invitations.domain.InvitationResponseStatus
import org.niikage.planr.features.invitations.domain.NamedInvitation
import org.niikage.planr.features.invitations.domain.UnnamedInvitation
import org.niikage.planr.features.invitations.repository.InvitationRepository
import org.niikage.planr.features.invitations.service.InvitationService
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.shared.exceptions.BadRequestException
import org.niikage.planr.shared.exceptions.ConflictException
import org.niikage.planr.shared.exceptions.NotFoundException
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class InvitationServiceImpl(
    private val repo: InvitationRepository,
    private val rabbitTemplate: RabbitTemplate,
    private val applicationScope: CoroutineScope
) : InvitationService {
    override suspend fun createUnnamedInvitation(invitation: UnnamedInvitation): UUID {
        repo.saveInvitation(invitation)
        return invitation.invitationId
    }

    override suspend fun sendInvitations(
        invitations: List<NamedInvitation>
    ) {
        applicationScope.launch {
            invitations.map { invitation ->
                async(Dispatchers.IO) {
                    repo.saveInvitation(invitation)

                    rabbitTemplate.convertAndSend(
                        RabbitConstants.NOTIFICATION_EXCHANGE,
                        invitation.routingKey,
                        invitation
                    )

                    invitation
                }
            }.awaitAll()
        }
    }

    override suspend fun answerNamedInvitation(
        invitationId: UUID,
        respondentId: UserId,
        status: InvitationResponseStatus,
        callback: suspend (invitation: Invitation) -> Boolean
    ): Boolean {
        val invitation = repo
            .findInvitationById(invitationId)
            ?: throw NotFoundException("Приглашение не найдено")

        if (invitation is UnnamedInvitation) return callback(invitation)

        val namedInvitation = invitation as NamedInvitation
        if (namedInvitation.responseStatus != InvitationResponseStatus.PENDING)
            throw ConflictException("Ответ на это приглашение уже был получен")

        if (namedInvitation.receiver.id != respondentId)
            throw BadRequestException("Вы не можете ответить на это приглашение")

        invitation.responseStatus = status
        invitation.respondedAt = OffsetDateTime.now()

        repo.saveInvitation(invitation)

        return callback(invitation)
    }
}