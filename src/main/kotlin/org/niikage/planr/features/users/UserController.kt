package org.niikage.planr.features.users

import org.niikage.planr.features.users.dto.UserCreateRequest
import org.niikage.planr.features.users.dto.UserUpdateRequest
import org.niikage.planr.features.users.dto.UserResponse
import org.niikage.planr.features.users.service.UserService
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.features.users.domain.UserSocials
import org.niikage.planr.features.users.dto.toResponse
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

@RestController
@RequestMapping("/api/users")
class UserController(
    private val service: UserService
) {
    // ==================== GET ====================
    @GetMapping
    suspend fun getUserById(
        @RequestParam userId: UUID?,
        @RequestParam tgId: String?,
        @RequestParam vkId: String?
    ): ResponseEntity<UserResponse> {
        if (userId == null && tgId == null && vkId == null) {
            return ResponseEntity.badRequest().build()
        }

        val user = when {
            userId != null -> service.getUser(UserId(userId))
            tgId != null -> service.getUser(UserSocials.ofTg(tgId))
            vkId != null -> service.getUser(UserSocials.ofVk(vkId))
            else -> return ResponseEntity.notFound().build()
        }

        return ResponseEntity.ok(user.toResponse())
    }

    // ==================== CREATE ====================
    @PostMapping
    suspend fun createUser(
        @RequestBody @Validated request: UserCreateRequest
    ): ResponseEntity<UserResponse> {
        val created = service.create(request)

        return ResponseEntity
            .created(URI("/api/users?userId=${created.id.value}"))
            .body(created.toResponse())
    }

    // ==================== UPDATE ====================
    @PatchMapping
    suspend fun updateUser(
        @RequestBody @Validated request: UserUpdateRequest
    ): ResponseEntity<UserResponse> {
        val updated = service.update(request)
        return ResponseEntity.ok(updated.toResponse())
    }

    // ==================== DELETE ====================
    @DeleteMapping("/{userId}")
    suspend fun deleteUser(
        @PathVariable userId: UUID
    ) {
        service.delete(UserId(userId))
    }
}