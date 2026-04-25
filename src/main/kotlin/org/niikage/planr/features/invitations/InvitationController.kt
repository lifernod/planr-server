package org.niikage.planr.features.invitations

import org.niikage.planr.features.eventparticipants.service.EventParticipantService
import org.niikage.planr.features.events.domain.toEventId
import org.niikage.planr.features.invitations.domain.EventInvitationTarget
import org.niikage.planr.features.invitations.domain.InvitationResponseStatus
import org.niikage.planr.features.invitations.service.InvitationService
import org.niikage.planr.features.recentcontacts.service.RecentContactService
import org.niikage.planr.features.users.domain.toUserId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/api/invitations")
class InvitationController (
    private val service: InvitationService,
    private val participantService: EventParticipantService,
    private val contactService: RecentContactService
){
    @GetMapping("/{invitationId}/answer")
    suspend fun answerInvitation(
        @PathVariable invitationId: UUID,
        @RequestParam(defaultValue = "ACCEPTED") status: InvitationResponseStatus,
        principal: Principal
    ): ResponseEntity<String> {
        service.answerInvitation(
            invitationId,
            principal.toUserId(),
            status
        ) {
           when (it.target) {
               is EventInvitationTarget -> participantService.addParticipant(
                   (it.target as EventInvitationTarget).event.id.toEventId(),
                   principal.toUserId()
               )
           }

            contactService.addOrRefresh(it.sender.id.toUserId(), principal.toUserId())

            true
        }

        val message = when(status) {
            InvitationResponseStatus.ACCEPTED -> "Приглашение принято"
            InvitationResponseStatus.DECLINED -> "Приглашение отклонено"
            InvitationResponseStatus.PENDING -> "Приглашение проигнорировано"
        };

        return ResponseEntity.ok(message)
    }
}