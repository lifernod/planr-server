package org.niikage.planr.features.users.query

import kotlinx.coroutines.reactor.awaitSingle
import org.niikage.planr.shared.exceptions.NotFoundException
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitSingleOrNull
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class UserQueryRepository(
    private val databaseClient: DatabaseClient,
) {
    suspend fun findById(userId: UUID): UserView {
        val sql = """
            SELECT 
                id AS user_id,
                name AS user_name,
                tg_id IS NOT NULL AS user_tg_connected,
                vk_id IS NOT NULL AS user_vk_connected,
                created_at AS user_created_at
            FROM users WHERE id = :userId
        """.trimIndent()

        return databaseClient
            .sql(sql)
            .bind("userId", userId)
            .map { row, _ -> row.mapUser() }
            .awaitSingleOrNull()
            ?: throw NotFoundException("Пользователь не найден")
    }

    suspend fun findAllByIds(userIds: List<UUID>): Map<UUID, UserView> {
        val ids = userIds.distinct().toTypedArray()

        val sql = """
            SELECT
                id AS user_id,
                name AS user_name,
                tg_id IS NOT NULL AS user_tg_connected,
                vk_id IS NOT NULL AS user_vk_connected,
                created_at AS user_created_at
            FROM users WHERE id = ANY(:userIds)
        """.trimIndent()

        return databaseClient
            .sql(sql)
            .bind("userIds", ids)
            .map { row, _ -> row.mapUser() }
            .all()
            .collectMap(UserView::id)
            .awaitSingle()
    }
}