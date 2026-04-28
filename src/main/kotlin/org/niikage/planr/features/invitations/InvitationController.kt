package org.niikage.planr.features.invitations

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.niikage.planr.features.eventparticipants.service.EventParticipantService
import org.niikage.planr.features.events.domain.toEventId
import org.niikage.planr.features.invitations.domain.EventInvitationTarget
import org.niikage.planr.features.invitations.domain.InvitationResponseStatus
import org.niikage.planr.features.invitations.service.InvitationService
import org.niikage.planr.features.recentcontacts.service.RecentContactService
import org.niikage.planr.features.users.domain.toUserId
import org.niikage.planr.shared.exceptions.ApiExceptionResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/api/invitations")
@Tag(
    name = "Приглашения",
    description = "Управление приглашениями на события"
)
class InvitationController(
    private val service: InvitationService,
    private val participantService: EventParticipantService,
    private val contactService: RecentContactService
) {
    @GetMapping("/{invitationId}/answer")
    @Operation(
        summary = "Ответить на приглашение",
        description = """
            Позволяет пользователю принять или отклонить приглашение на событие.
            После ответа автоматически добавляется участник к событию (если принято)
            и обновляется список недавних контактов.
        """,
        parameters = [
            Parameter(
                name = "invitationId",
                description = "Уникальный идентификатор приглашения",
                required = true,
                `in` = ParameterIn.PATH,
                schema = Schema(type = "string", format = "uuid")
            ),
            Parameter(
                name = "status",
                description = "Статус ответа на приглашение",
                required = false,
                `in` = ParameterIn.QUERY,
                schema = Schema(type = "string", allowableValues = ["ACCEPTED", "DECLINED", "PENDING"])
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Приглашение успешно обработано",
                content = [Content(schema = Schema(implementation = String::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Приглашение не найдено",
                content = [Content(schema = Schema(implementation = ApiExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "409",
                description = "Приглашение уже было отвечено ранее",
                content = [Content(schema = Schema(implementation = ApiExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Пользователь не авторизован для ответа на это приглашение",
                content = [Content(schema = Schema(implementation = ApiExceptionResponse::class))]
            )
        ]
    )
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

        val message = when (status) {
            InvitationResponseStatus.ACCEPTED -> "Приглашение принято"
            InvitationResponseStatus.DECLINED -> "Приглашение отклонено"
            InvitationResponseStatus.PENDING -> "Приглашение проигнорировано"
        }

        return ResponseEntity.ok(message)
    }
}