package org.niikage.planr.features.eventparticipants

import org.niikage.planr.features.eventparticipants.query.EventParticipantQueryRepository
import org.niikage.planr.features.eventparticipants.query.entity.EventParticipantsList
import org.niikage.planr.shared.kernel.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/events/participants")
class EventParticipantController(
    private val repo: EventParticipantQueryRepository
) {
    // ==================== GET ====================
    @GetMapping("/{eventId}")
    suspend fun getEventParticipants(
        @PathVariable eventId: UUID,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int
    ): ResponseEntity<EventParticipantsList> {
        val participants = repo.findAllByEventId(eventId, PageRequest(limit, offset))
        return ResponseEntity.ok(participants)
    }
}