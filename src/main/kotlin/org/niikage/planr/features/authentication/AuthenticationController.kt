package org.niikage.planr.features.authentication

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.niikage.planr.features.authentication.service.AuthenticationService
import org.niikage.planr.features.users.dto.UserCreateRequest
import org.niikage.planr.shared.exceptions.ApiExceptionResponse
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.niikage.planr.features.authentication.dto.TelegramAuthRequest
import org.niikage.planr.features.authentication.dto.TelegramUserDto
import org.niikage.planr.features.authentication.service.TelegramInitDataService
import org.springframework.http.HttpStatus

@RestController
@RequestMapping("/api/auth")
@Tag(
    name = "Авторизация",
    description = "Вход в аккаунт или создание аккаунта"
)
class AuthenticationController(
    private val authenticationService: AuthenticationService,
    private val telegramInitDataService: TelegramInitDataService
) {
    @GetMapping("/sign-in")
    @Operation(
        summary = "Вход в аккаунт",
        description = """
            Вход в аккаунт с помощью социальных сетей.
            Войти можно либо с помощью телеграм, либо с помощью ВК - обязательно должен
            быть указан один из ID пользователя в указанных социальных сетях.
        """,
        parameters = [
            Parameter(
                name = "tgId",
                description = "ID пользователя в телеграм",
                required = false,
                `in` = ParameterIn.QUERY
            ),
            Parameter(
                name = "vkId",
                description = "ID пользователя в ВК",
                required = false,
                `in` = ParameterIn.QUERY
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Пользователь успешно авторизован",
                content = [Content(schema = Schema(implementation = String::class, description = "Токен авторизации"))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Пользователь с указанным ID соц.сети не найден",
                content = [Content(schema = Schema(implementation = ApiExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Не указан ни один ID социальной сети",
                content = [Content(schema = Schema(implementation = ApiExceptionResponse::class))]
            )
        ]
    )
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


    @PostMapping("/sign-up")
    @Operation(
        summary = "Регистрация нового пользователя",
        description = """
            Создание нового аккаунта пользователя.
            Требуется указать имя пользователя и хотя бы одну социальную сеть (Telegram или VK).
        """,
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для регистрации пользователя",
            required = true,
            content = [Content(schema = Schema(implementation = UserCreateRequest::class))]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Пользователь успешно зарегистрирован",
                content = [Content(schema = Schema(implementation = String::class, description = "Токен авторизации"))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Некорректные данные запроса или не указана социальная сеть",
                content = [Content(schema = Schema(implementation = ApiExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "409",
                description = "Пользователь с такими социальными сетями уже существует",
                content = [Content(schema = Schema(implementation = ApiExceptionResponse::class))]
            )
        ]
    )
    suspend fun signUp(@RequestBody @Validated request: UserCreateRequest): ResponseEntity<String> {
        val token = authenticationService.signUp(request)
        return ResponseEntity.ok(token)
    }
    @PostMapping("/telegram")
    @Operation(
        summary = "Вход через Telegram initData",
        description = """
            Аутентификация через Telegram Mini App.
            Принимает initData из Telegram.WebApp.initData, валидирует подпись HMAC-SHA256,
            и возвращает JWT если пользователь найден, или 404 с данными пользователя для регистрации.
        """,
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "initData из Telegram.WebApp.initData",
            required = true,
            content = [Content(schema = Schema(implementation = TelegramAuthRequest::class))]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Пользователь найден — возвращает JWT токен",
                content = [Content(schema = Schema(implementation = String::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Пользователь не найден — возвращает данные из Telegram для регистрации",
                content = [Content(schema = Schema(implementation = TelegramUserDto::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Неверная подпись или устаревший initData",
                content = [Content(schema = Schema(implementation = ApiExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Некорректный формат initData",
                content = [Content(schema = Schema(implementation = ApiExceptionResponse::class))]
            )
        ]
    )
    suspend fun authViaTelegram(
        @RequestBody request: TelegramAuthRequest
    ): ResponseEntity<Any> {
        val tgUser = telegramInitDataService.validateAndParseInitData(request.initData)
        val token = authenticationService.signInWithTelegramInitData(tgUser)

        return if (token != null) {
            ResponseEntity.ok(token)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(tgUser)
        }
    }
}