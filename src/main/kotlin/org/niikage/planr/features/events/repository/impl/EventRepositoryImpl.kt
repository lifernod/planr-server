package org.niikage.planr.features.events.repository.impl

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitSingle
import org.niikage.planr.features.events.domain.*
import org.niikage.planr.features.events.repository.EventRepository
import org.niikage.planr.features.events.repository.data.EventDataRepository
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.shared.kernel.PageRequest
import org.niikage.planr.shared.utils.optional
import org.niikage.planr.shared.utils.required
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
class EventRepositoryImpl(
    private val repo: EventDataRepository,
    private val databaseClient: DatabaseClient,
) : EventRepository {
    // ==================== GET ====================
    override suspend fun findById(id: EventId): EventDomain? {
        return repo.findById(id.value)?.toDomain()
    }

    override suspend fun findAllByCreatorId(
        creatorId: UserId,
        pageRequest: PageRequest
    ): List<EventDomain> {
        return repo.findAllByCreatorId(
            creatorId.value,
            limit = pageRequest.limit,
            offset = pageRequest.offset
        ).map { it.toDomain() }.toList()
    }

    // ==================== CREATE ====================
    override suspend fun createEvent(event: EventDomain): EventDomain {
        return repo.save(event.toEntity()).toDomain()
    }

    // ==================== UPDATE ====================
    override suspend fun updateEvent(event: EventDomain): EventDomain {
        return repo.save(event.toEntity()).toDomain()
    }

    // ==================== DELETE ====================
    override suspend fun deleteById(id: EventId) {
        repo.deleteById(id.value)
    }

    // ==================== MANUAL QUERIES ====================
    override suspend fun findAllByUserId(
        userId: UserId,
        pageRequest: PageRequest
    ): List<EventDomain> {
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
            WHERE ep.user_id = :userId AND role = 'PARTICIPANT'
            LIMIT :limit OFFSET :offset
        """.trimIndent()

        val events = databaseClient
            .sql(sql)
            .bind("userId", userId)
            .bind("limit", pageRequest.limit)
            .bind("offset", pageRequest.offset)
            .map { row, _ -> mapEventDomain(row) }
            .all()
            .collectList()
            .awaitSingle()

        return events
    }

    private fun mapEventDomain(row: Row): EventDomain {
        return EventDomain(
            id = row.required("event_id"),
            creatorId = row.required("event_creator_id"),
            title = row.required("event_title"),
            type = EventType.valueOf(row.required("event_type")),
            description = row.optional("event_description"),
            location = row.required("event_location"),
            startTime = row.required("event_start_time"),
            endTime = row.optional("event_end_time"),
            createdAt = row.required("event_created_at")
        )
    }
}