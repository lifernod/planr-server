package org.niikage.planr.features.events

import org.niikage.planr.features.events.domain.toEventId
import org.niikage.planr.features.events.dto.EventCreateRequest
import org.niikage.planr.features.events.dto.EventResponse
import org.niikage.planr.features.events.dto.EventUpdateRequest
import org.niikage.planr.features.events.dto.toResponse
import org.niikage.planr.features.events.service.EventService
import org.niikage.planr.features.users.domain.toUserId
import org.niikage.planr.shared.kernel.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/api/events")
class EventController(
    private val service: EventService
) {
    // ==================== GET ====================
    @GetMapping("/{eventId}")
    suspend fun getEventById(
        @PathVariable eventId: UUID
    ): ResponseEntity<EventResponse> {
        val user = service.getEvent(eventId.toEventId())
        return ResponseEntity.ok(user.toResponse())
    }

    @GetMapping("/created")
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
    suspend fun updateEvent(
        @PathVariable eventId: UUID,
        @RequestBody @Validated request: EventUpdateRequest
    ): ResponseEntity<EventResponse> {
        val updated = service.update(eventId.toEventId(), request)
        return ResponseEntity.ok(updated.toResponse())
    }

    // ==================== DELETE ====================
    @DeleteMapping("/{eventId}")
    suspend fun deleteEvent(
        @PathVariable eventId: UUID
    ) {
        service.delete(eventId.toEventId())
    }
}