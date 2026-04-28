package org.niikage.planr.features.eventparticipants

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.niikage.planr.features.eventparticipants.query.EventParticipantsList
import org.niikage.planr.features.eventparticipants.service.EventParticipantService
import org.niikage.planr.features.events.domain.toEventId
import org.niikage.planr.features.users.domain.toUserId
import org.niikage.planr.shared.exceptions.ApiExceptionResponse
import org.niikage.planr.shared.kernel.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/api/events/participants")
@Tag(
    name = "Участники события",
    description = "Управление участниками события"
)
class EventParticipantController(
    private val service: EventParticipantService,
) {
    // ==================== GET ====================
    @GetMapping("/{eventId}")
    @Operation(
        summary = "Получить участников события",
        description = "Возвращает список участников указанного события",
        parameters = [
            Parameter(
                name = "eventId",
                description = "Уникальный идентификатор события",
                required = true,
                `in` = ParameterIn.PATH,
                schema = Schema(type = "string", format = "uuid")
            ),
            Parameter(
                name = "limit",
                description = "Максимальное количество участников для возврата",
                required = false,
                `in` = ParameterIn.QUERY,
                schema = Schema(type = "integer", defaultValue = "20")
            ),
            Parameter(
                name = "offset",
                description = "Количество участников для пропуска (смещение)",
                required = false,
                `in` = ParameterIn.QUERY,
                schema = Schema(type = "integer", defaultValue = "0")
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Список участников события",
                content = [Content(schema = Schema(implementation = EventParticipantsList::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Событие не найдено",
                content = [Content(schema = Schema(implementation = ApiExceptionResponse::class))]
            )
        ]
    )
    suspend fun getEventParticipants(
        @PathVariable eventId: UUID,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int
    ): ResponseEntity<EventParticipantsList> {
        val participants = service.getEventParticipants(eventId.toEventId(), PageRequest(limit, offset))
        return ResponseEntity.ok(participants)
    }

    // ==================== PUT ====================
    @PutMapping("/{eventId}")
    @Operation(
        summary = "Приглашение участников события",
        description = "Отправляет указанным пользователям именованные приглашения",
        parameters = [
            Parameter(
                name = "eventId",
                description = "Уникальный идентификатор события",
                required = true,
                `in` = ParameterIn.PATH,
                schema = Schema(type = "string", format = "uuid")
            ),
            Parameter(
                name = "limit",
                description = "Максимальное количество участников для возврата",
                required = false,
                `in` = ParameterIn.QUERY,
                schema = Schema(type = "integer", defaultValue = "20")
            )
        ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Список пользователей, которых следует пригласить на событие",
            required = true,
            content = [Content(schema = Schema(implementation = Array<UUID>::class))]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Информация о количестве приглашенных пользователей",
                content = [Content(schema = Schema(implementation = String::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Событие не найдено",
                content = [Content(schema = Schema(implementation = ApiExceptionResponse::class))]
            )
        ]
    )
    suspend fun addParticipants(
        @PathVariable eventId: UUID,
        @RequestBody users: List<UUID>,
        principal: Principal
    ): ResponseEntity<String> {
        val ids = users.distinct().filter { it.toUserId() != principal.toUserId() }
        if (ids.isEmpty()) return ResponseEntity.badRequest().body("Не указаны пользователи для добавления в участники")

        val added = service.inviteParticipants(eventId.toEventId(), ids.map { it.toUserId() })

        return ResponseEntity.ok("Добавлено участников: ${added.size}/${users.size}")
    }

    // ==================== DELETE ====================
    @DeleteMapping("/{eventId}")
    @Operation(
        summary = "Исключение участников события",
        description = "Удаляет указанных пользователей из списка участников события",
        parameters = [
            Parameter(
                name = "eventId",
                description = "Уникальный идентификатор события",
                required = true,
                `in` = ParameterIn.PATH,
                schema = Schema(type = "string", format = "uuid")
            ),
            Parameter(
                name = "limit",
                description = "Максимальное количество участников для возврата",
                required = false,
                `in` = ParameterIn.QUERY,
                schema = Schema(type = "integer", defaultValue = "20")
            )
        ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Список пользователей, которых следует исключить из участников события",
            required = true,
            content = [Content(schema = Schema(implementation = Array<UUID>::class))]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Информация о количестве исключенных пользователей",
                content = [Content(schema = Schema(implementation = String::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Событие не найдено",
                content = [Content(schema = Schema(implementation = ApiExceptionResponse::class))]
            )
        ]
    )
    suspend fun removeParticipants(
        @PathVariable eventId: UUID,
        @RequestBody users: List<UUID>,
        principal: Principal
    ): ResponseEntity<String> {
        val ids = users.distinct().filter { it.toUserId() != principal.toUserId() }
        if (ids.isEmpty()) return ResponseEntity.badRequest().body("Не указаны пользователи для удаления из участников")

        val removed = if (ids.size == 1) {
            service.removeParticipant(eventId.toEventId(), ids.first().toUserId())
            1
        } else {
            service.removeParticipants(eventId.toEventId(), ids.map { it.toUserId() })
        }

        return ResponseEntity.ok("Удалено участников: ${removed}/${users.size}")
    }
}