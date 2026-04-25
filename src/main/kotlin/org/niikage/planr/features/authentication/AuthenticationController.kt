package org.niikage.planr.features.authentication

import org.niikage.planr.features.users.dto.UserCreateRequest
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthenticationController(
    private val authenticationService: AuthenticationService
) {
    @GetMapping("/sign-in")
    suspend fun signIn(
        @RequestParam tgId: String?,
        @RequestParam vkId: String?
    ): ResponseEntity<String> {
        val token = when {
            tgId != null -> authenticationService.signInWithTg(tgId)
            vkId != null -> authenticationService.signInWithVk(vkId)
            else -> return ResponseEntity.notFound().build()
        }

        return ResponseEntity.ok(token)
    }


    @PostMapping("/signup")
    suspend fun signUp(@RequestBody @Validated request: UserCreateRequest): ResponseEntity<String> {
        val token = authenticationService.signUp(request)
        return ResponseEntity.ok(token)
    }
}