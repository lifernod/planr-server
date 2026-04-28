package org.niikage.planr.features.users

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.niikage.planr.features.users.domain.toUserId
import org.niikage.planr.features.users.dto.UserResponse
import org.niikage.planr.features.users.dto.UserUpdateRequest
import org.niikage.planr.features.users.dto.toResponse
import org.niikage.planr.features.users.service.UserService
import org.niikage.planr.shared.exceptions.ApiExceptionResponse
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/users")
@Tag(
    name = "Пользователи",
    description = "Управление профилем пользователя"
)
class UserController(
    private val service: UserService
) {
    // ==================== GET ====================
    @GetMapping
    @Operation(
        summary = "Получить профиль пользователя",
        description = "Возвращает информацию о текущем авторизованном пользователе.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Профиль пользователя",
                content = [Content(schema = Schema(implementation = UserResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Пользователь не найден",
                content = [Content(schema = Schema(implementation = ApiExceptionResponse::class))]
            )
        ]
    )
    suspend fun getUserById(
        principal: Principal
    ): ResponseEntity<UserResponse> {
        val user = service.getUser(principal.toUserId())
        return ResponseEntity.ok(user.toResponse())
    }

    // ==================== UPDATE ====================
    @PatchMapping
    @Operation(
        summary = "Обновить профиль пользователя",
        description = "Обновляет информацию о текущем авторизованном пользователе.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для обновления профиля",
            required = true,
            content = [Content(schema = Schema(implementation = UserUpdateRequest::class))]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Профиль успешно обновлен",
                content = [Content(schema = Schema(implementation = UserResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Некорректные данные запроса",
                content = [Content(schema = Schema(implementation = ApiExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Пользователь не найден",
                content = [Content(schema = Schema(implementation = ApiExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "409",
                description = "Конфликт при обновлении профиля",
                content = [Content(schema = Schema(implementation = ApiExceptionResponse::class))]
            )
        ]
    )
    suspend fun updateUser(
        @RequestBody @Validated request: UserUpdateRequest,
        principal: Principal
    ): ResponseEntity<UserResponse> {
        val updated = service.update(principal.toUserId(), request)
        return ResponseEntity.ok(updated.toResponse())
    }

    // ==================== DELETE ====================
    @DeleteMapping
    @Operation(
        summary = "Удалить профиль пользователя",
        description = "Удаляет профиль текущего авторизованного пользователя и все связанные данные.",
        responses = [
            ApiResponse(
                responseCode = "204",
                description = "Профиль успешно удален"
            )
        ]
    )
    suspend fun deleteUser(principal: Principal) {
        service.delete(principal.toUserId())
    }
}