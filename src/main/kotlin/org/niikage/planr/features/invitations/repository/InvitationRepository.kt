package org.niikage.planr.features.invitations.repository

import org.niikage.planr.features.invitations.domain.Invitation
import java.util.UUID

interface InvitationRepository {
    suspend fun findInvitationById(invitationId: UUID): Invitation?

    suspend fun saveInvitation(invitation: Invitation)
}