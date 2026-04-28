package org.niikage.planr.features.events

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.niikage.planr.features.events.domain.toEventId
import org.niikage.planr.features.events.dto.EventCreateRequest
import org.niikage.planr.features.events.dto.EventResponse
import org.niikage.planr.features.events.dto.EventUpdateRequest
import org.niikage.planr.features.events.dto.toResponse
import org.niikage.planr.features.events.service.EventService
import org.niikage.planr.features.users.domain.toUserId
import org.niikage.planr.shared.exceptions.ApiExceptionResponse
import org.niikage.planr.shared.kernel.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/api/events")
@Tag(
    name = "События",
    description = "Управление событиями: создание, получение, обновление и удаление"
)
class EventController(
    private val service: EventService
) {
    // ==================== GET ====================
    @GetMapping("/{eventId}")
    @Operation(
        summary = "Получить событие по ID",
        description = "Возвращает информацию о событии по его уникальному идентификатору.",
        parameters = [
            Parameter(
                name = "eventId",
                description = "Уникальный идентификатор события",
                required = true,
                `in` = ParameterIn.PATH,
                schema = Schema(type = "string", format = "uuid")
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Событие найдено",
                content = [Content(schema = Schema(implementation = EventResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Событие не найдено",
                content = [Content(schema = Schema(implementation = ApiExceptionResponse::class))]
            )
        ]
    )
    suspend fun getEventById(
        @PathVariable eventId: UUID
    ): ResponseEntity<EventResponse> {
        val user = service.getEvent(eventId.toEventId())
        return ResponseEntity.ok(user.toResponse())
    }

    @GetMapping("/created")
    @Operation(
        summary = "Получить созданные события",
        description = "Возвращает список событий, созданных текущим пользователем.",
        parameters = [
            Parameter(
                name = "limit",
                description = "Максимальное количество событий для возврата",
                required = false,
                `in` = ParameterIn.QUERY,
                schema = Schema(type = "integer", defaultValue = "20")
            ),
            Parameter(
                name = "offset",
                description = "Количество событий для пропуска (смещение)",
                required = false,
                `in` = ParameterIn.QUERY,
                schema = Schema(type = "integer", defaultValue = "0")
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Список созданных событий",
                content = [Content(schema = Schema(implementation = Array<EventResponse>::class))]
            )
        ]
    )
    suspend fun getCreatedEvents(
        @RequestParam(defaultValue = "20") limit: Int = 20,
        @RequestParam(defaultValue = "0") offset: Int = 0,
        principal: Principal
    ): ResponseEntity<List<EventResponse>> {
        val events = service.getCreatedEvents(
            principal.toUserId(),
            PageRequest(limit, offset)
        )
        return ResponseEntity.ok(events.map { it.toResponse() })
    }

    @GetMapping("/participated")
    @Operation(
        summary = "Получить события с участием",
        description = "Возвращает список событий, в которых текущий пользователь является участником.",
        parameters = [
            Parameter(
                name = "limit",
                description = "Максимальное количество событий для возврата",
                required = false,
                `in` = ParameterIn.QUERY,
                schema = Schema(type = "integer", defaultValue = "20")
            ),
            Parameter(
                name = "offset",
                description = "Количество событий для пропуска (смещение)",
                required = false,
                `in` = ParameterIn.QUERY,
                schema = Schema(type = "integer", defaultValue = "0")
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Список событий с участием",
                content = [Content(schema = Schema(implementation = Array<EventResponse>::class))]
            )
        ]
    )
    suspend fun getParticipatedEvents(
        @RequestParam(defaultValue = "20") limit: Int = 20,
        @RequestParam(defaultValue = "0") offset: Int = 0,
        principal: Principal
    ): ResponseEntity<List<EventResponse>> {
        val events = service.getParticipatedEvents(
            principal.toUserId(),
            PageRequest(limit, offset)
        )
        return ResponseEntity.ok(events.map { it.toResponse() })
    }

    // ==================== CREATE ====================
    @PostMapping
    @Operation(
        summary = "Создать новое событие",
        description = "Создает новое событие. Текущий пользователь автоматически становится создателем и участником события.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для создания события",
            required = true,
            content = [Content(schema = Schema(implementation = EventCreateRequest::class))]
        ),
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "Событие успешно создано",
                content = [Content(schema = Schema(implementation = EventResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Некорректные данные запроса",
                content = [Content(schema = Schema(implementation = ApiExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Пользователь-создатель не найден",
                content = [Content(schema = Schema(implementation = ApiExceptionResponse::class))]
            )
        ]
    )
    suspend fun createEvent(
        @RequestBody @Validated request: EventCreateRequest,
        principal: Principal
    ): ResponseEntity<EventResponse> {
        val created = service.create(principal.toUserId(), request)
        return ResponseEntity
            .created(URI("/api/events/${created.id.value}"))
            .body(created.toResponse())
    }

    // ==================== UPDATE ====================
    @PatchMapping("/{eventId}")
    @Operation(
        summary = "Обновить событие",
        description = "Обновляет существующее событие. Только создатель события может его обновлять.",
        parameters = [
            Parameter(
                name = "eventId",
                description = "Уникальный идентификатор события для обновления",
                required = true,
                `in` = ParameterIn.PATH,
                schema = Schema(type = "string", format = "uuid")
            )
        ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для обновления события",
            required = true,
            content = [Content(schema = Schema(implementation = EventUpdateRequest::class))]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Событие успешно обновлено",
                content = [Content(schema = Schema(implementation = EventResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Некорректные данные запроса",
                content = [Content(schema = Schema(implementation = ApiExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Пользователь не является создателем события",
                content = [Content(schema = Schema(implementation = ApiExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Событие не найдено",
                content = [Content(schema = Schema(implementation = ApiExceptionResponse::class))]
            )
        ]
    )
    suspend fun updateEvent(
        @PathVariable eventId: UUID,
        @RequestBody @Validated request: EventUpdateRequest,
        principal: Principal
    ): ResponseEntity<EventResponse> {
        val updated = service.update(eventId.toEventId(), principal.toUserId(), request)
        return ResponseEntity.ok(updated.toResponse())
    }

    // ==================== DELETE ====================
    @DeleteMapping("/{eventId}")
    @Operation(
        summary = "Удалить событие",
        description = "Удаляет событие и все связанные с ним данные.",
        parameters = [
            Parameter(
                name = "eventId",
                description = "Уникальный идентификатор события для удаления",
                required = true,
                `in` = ParameterIn.PATH,
                schema = Schema(type = "string", format = "uuid")
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "204",
                description = "Событие успешно удалено"
            )
        ]
    )
    suspend fun deleteEvent(
        @PathVariable eventId: UUID
    ) {
        service.delete(eventId.toEventId())
    }
}