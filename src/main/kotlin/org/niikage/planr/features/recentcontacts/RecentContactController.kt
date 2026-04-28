package org.niikage.planr.features.recentcontacts

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.niikage.planr.features.recentcontacts.domain.RecentContactsList
import org.niikage.planr.features.recentcontacts.service.RecentContactService
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.features.users.domain.toUserId
import org.niikage.planr.shared.kernel.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/api/recent-contacts")
@Tag(
    name = "Недавние контакты",
    description = "Управление списком недавних контактов пользователя"
)
class RecentContactController(
    private val service: RecentContactService
) {
    @GetMapping
    @Operation(
        summary = "Получить недавние контакты",
        description = "Возвращает список недавних контактов текущего пользователя.",
        parameters = [
            Parameter(
                name = "limit",
                description = "Максимальное количество контактов для возврата",
                required = false,
                `in` = ParameterIn.QUERY,
                schema = Schema(type = "integer", defaultValue = "20")
            ),
            Parameter(
                name = "offset",
                description = "Количество контактов для пропуска (смещение)",
                required = false,
                `in` = ParameterIn.QUERY,
                schema = Schema(type = "integer", defaultValue = "0")
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Список недавних контактов",
                content = [Content(schema = Schema(implementation = RecentContactsList::class))]
            )
        ]
    )
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
    @Operation(
        summary = "Удалить контакт",
        description = "Удаляет указанный контакт из списка недавних контактов.",
        parameters = [
            Parameter(
                name = "contactId",
                description = "Уникальный идентификатор контакта для удаления",
                required = true,
                `in` = ParameterIn.QUERY,
                schema = Schema(type = "string", format = "uuid")
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Контакт успешно удален"
            )
        ]
    )
    suspend fun removeRecentContact(
        @RequestParam("contactId") contactId: UUID,
        principal: Principal
    ) {
        service.delete(principal.toUserId(), UserId(contactId))
    }

    @DeleteMapping("/remove-all")
    @Operation(
        summary = "Удалить все контакты",
        description = "Удаляет все контакты из списка недавних контактов текущего пользователя.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Все контакты успешно удалены"
            )
        ]
    )
    suspend fun removeRecentContacts(
        principal: Principal
    ) {
        service.deleteAllByOwnerId(principal.toUserId())
    }
}