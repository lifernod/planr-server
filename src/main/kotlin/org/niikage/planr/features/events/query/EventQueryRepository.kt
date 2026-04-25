package org.niikage.planr.features.events.query

import org.niikage.planr.features.users.query.UserQueryRepository
import org.niikage.planr.shared.exceptions.NotFoundException
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
}