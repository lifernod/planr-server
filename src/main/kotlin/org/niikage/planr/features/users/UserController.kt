package org.niikage.planr.features.users

import org.niikage.planr.features.users.domain.toUserId
import org.niikage.planr.features.users.dto.UserResponse
import org.niikage.planr.features.users.dto.UserUpdateRequest
import org.niikage.planr.features.users.dto.toResponse
import org.niikage.planr.features.users.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/users")
class UserController(
    private val service: UserService
) {
    // ==================== GET ====================
    @GetMapping
    suspend fun getUserById(
        principal: Principal
    ): ResponseEntity<UserResponse> {
        val user = service.getUser(principal.toUserId())
        return ResponseEntity.ok(user.toResponse())
    }

    // ==================== UPDATE ====================
    @PatchMapping
    suspend fun updateUser(
        @RequestBody @Validated request: UserUpdateRequest,
        principal: Principal
    ): ResponseEntity<UserResponse> {
        val updated = service.update(principal.toUserId(), request)
        return ResponseEntity.ok(updated.toResponse())
    }

    // ==================== DELETE ====================
    @DeleteMapping
    suspend fun deleteUser(principal: Principal) {
        service.delete(principal.toUserId())
    }
}