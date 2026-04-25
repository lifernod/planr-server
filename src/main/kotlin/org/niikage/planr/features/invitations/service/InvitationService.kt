package org.niikage.planr.features.invitations.service

import org.niikage.planr.features.invitations.domain.Invitation
import org.niikage.planr.features.invitations.domain.InvitationResponseStatus
import org.niikage.planr.features.invitations.domain.NamedInvitation
import org.niikage.planr.features.invitations.domain.UnnamedInvitation
import org.niikage.planr.features.users.domain.UserId
import java.util.UUID

interface InvitationService {
    suspend fun createUnnamedInvitation(invitation: UnnamedInvitation): UUID

    suspend fun sendInvitations(invitations: List<NamedInvitation>)

    suspend fun answerNamedInvitation(
        invitationId: UUID,
        respondentId: UserId,
        status: InvitationResponseStatus = InvitationResponseStatus.ACCEPTED,
        callback: suspend (invitation: Invitation) -> Boolean
    ): Boolean
}