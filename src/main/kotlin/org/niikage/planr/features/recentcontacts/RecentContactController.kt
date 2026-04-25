package org.niikage.planr.features.recentcontacts

import org.niikage.planr.features.recentcontacts.domain.RecentContactsList
import org.niikage.planr.features.recentcontacts.service.RecentContactService
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.features.users.domain.toUserId
import org.niikage.planr.shared.kernel.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/api/recent-contacts")
class RecentContactController(
    private val service: RecentContactService
) {
    @GetMapping
    suspend fun getRecentContacts(
        @RequestParam(defaultValue = "20") limit: Int = 20,
        @RequestParam(defaultValue = "0") offset: Int = 0,
        principal: Principal
    ): ResponseEntity<RecentContactsList> {
        val recentContacts = service.findAllByOwnerId(principal.toUserId(), PageRequest(limit, offset))
        return ResponseEntity
            .ok(recentContacts)
    }

    @PatchMapping("/remove")
    suspend fun removeRecentContact(
        @RequestParam("contactId") contactId: UUID,
        principal: Principal
    ) {
        service.delete(principal.toUserId(), UserId(contactId))
    }

    @DeleteMapping("/remove-all")
    suspend fun removeRecentContacts(
        principal: Principal
    ) {
        service.deleteAllByOwnerId(principal.toUserId())
    }
}