package org.niikage.planr.features.events.query

import kotlinx.coroutines.reactive.awaitSingle
import org.niikage.planr.features.users.query.UserQueryRepository
import org.niikage.planr.shared.exceptions.NotFoundException
import org.niikage.planr.shared.kernel.PageRequest
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitSingleOrNull
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class EventQueryRepository(
    private val databaseClient: DatabaseClient,
    private val userQueryRepository: UserQueryRepository
) {
    suspend fun findById(eventId: UUID): EventView {
        val sql = """
            SELECT
                id AS event_id,
                title AS event_title,
                description AS event_description,
                location AS event_location,
                type AS event_type,
                start_time AS event_start_time,
                end_time AS event_end_time,
                creator_id AS event_creator_id,
                created_at AS event_created_at
            FROM events
            WHERE id = :eventId;
        """.trimIndent()

        val event = databaseClient
            .sql(sql)
            .bind("eventId", eventId)
            .map { row, _ -> row.mapEvent() }
            .awaitSingleOrNull()
            ?: throw NotFoundException("Событие не найдено")

        val creator = userQueryRepository.findById(event.creatorId)
        event.creator = creator
        return event
    }

    /**
     * Получение событий, в которых учавствует пользователь
     */
    suspend fun findAllByUserId(
        userId: UUID,
        pageRequest: PageRequest
    ): List<EventView> {
        val sql = """
            SELECT
                e.id AS event_id,
                e.title AS event_title,
                e.description AS event_description,
                e.location AS event_location,
                e.type AS event_type,
                e.start_time AS event_start_time,
                e.end_time AS event_end_time,
                e.creator_id AS event_creator_id,
                e.created_at AS event_created_at
            FROM event_participants ep
            JOIN events e ON ep.event_id = e.id
            WHERE ep.user_id = :userId
            LIMIT :limit OFFSET :offset
        """.trimIndent()

        val events = databaseClient
            .sql(sql)
            .bind("userId", userId)
            .bind("limit", pageRequest.limit)
            .bind("offset", pageRequest.offset)
            .map { row, _ -> row.mapEvent() }
            .all()
            .collectList()
            .awaitSingle()

        if (events.isEmpty()) return emptyList()

        val creators = userQueryRepository.findAllByIds(events.map { it.creatorId })
        return events.map {
            it.creator = creators[it.creatorId]
            it
        }
    }
}