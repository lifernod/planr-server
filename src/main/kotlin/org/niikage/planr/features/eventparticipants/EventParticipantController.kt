package org.niikage.planr.features.eventparticipants

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.niikage.planr.features.eventparticipants.query.EventParticipantsList
import org.niikage.planr.features.eventparticipants.service.EventParticipantService
import org.niikage.planr.features.events.domain.toEventId
import org.niikage.planr.features.recentcontacts.service.RecentContactService
import org.niikage.planr.features.users.domain.toUserId
import org.niikage.planr.shared.kernel.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/api/events/participants")
class EventParticipantController(
    private val service: EventParticipantService,
) {
    // ==================== GET ====================
    @GetMapping("/{eventId}")
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
            service.removeParticipants(eventId.toEventId(), ids.map{ it.toUserId() })
        }

        return ResponseEntity.ok("Удалено участников: ${removed}/${users.size}")
    }
}