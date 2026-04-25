package org.niikage.planr.features.eventparticipants.repository.impl

import kotlinx.coroutines.reactor.awaitSingle
import org.niikage.planr.features.eventparticipants.query.EventParticipantRole
import org.niikage.planr.features.eventparticipants.query.EventParticipantsList
import org.niikage.planr.features.eventparticipants.query.mapEventParticipant
import org.niikage.planr.features.eventparticipants.repository.EventParticipantRepository
import org.niikage.planr.features.events.domain.EventId
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.features.users.query.UserQueryRepository
import org.niikage.planr.shared.exceptions.ConflictException
import org.niikage.planr.shared.kernel.PageRequest
import org.niikage.planr.shared.utils.required
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitSingleOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class EventParticipantRepositoryImpl(
    private val databaseClient: DatabaseClient,
    private val userQueryRepository: UserQueryRepository
) : EventParticipantRepository {
    // ==================== GET ====================
    override suspend fun findAllByEventId(
        eventId: EventId,
        pageRequest: PageRequest,
        role: EventParticipantRole
    ): EventParticipantsList {
        val sql = """
            SELECT 
                event_id,
                user_id AS participant_id,
                role AS participant_role
            FROM event_participants
            WHERE event_id = :eventId AND role = :role
            LIMIT :limit OFFSET :offset
        """.trimIndent()

        val participants = databaseClient
            .sql(sql)
            .bind("eventId", eventId.value)
            .bind("role", role.name)
            .bind("limit", pageRequest.limit)
            .bind("offset", pageRequest.offset)
            .map { row, _ -> row.mapEventParticipant() }
            .all()
            .collectList()
            .awaitSingle()

        if (participants.isEmpty()) return EventParticipantsList.empty()

        val users = userQueryRepository
            .findAllByIds(participants.map { it.participantId })

        return EventParticipantsList(
            count = users.size,
            participants = participants.map {
                it.participant = users[it.participantId]
                it
            },
        )
    }

    // ==================== ADD ====================
    override suspend fun addParticipant(
        eventId: EventId,
        userId: UserId,
        role: EventParticipantRole
    ): UserId {
        val sql = """
            INSERT INTO event_participants(event_id, user_id, role)
            VALUES (:eventId, :userId, :role)
            ON CONFLICT DO NOTHING
            RETURNING user_id
        """.trimIndent()

        val insertedId = databaseClient
            .sql(sql)
            .bind("eventId", eventId.value)
            .bind("userId", userId.value)
            .bind("role", role.name)
            .map { row, _ -> UserId(row.required<UUID>("user_id")) }
            .awaitSingleOrNull()

        if (insertedId == null)
            throw ConflictException("Пользователь уже является участником события")

        return insertedId
    }

    override suspend fun addParticipants(
        eventId: EventId,
        userIds: List<UserId>,
    ): List<UserId> {
        val ids = userIds.distinct().map { it.value }.toTypedArray()

        val sql = """
            INSERT INTO event_participants(event_id, user_id, role)
            SELECT :eventId, u.user_id FROM UNNEST(:userIds) AS u(user_id)
            ON CONFLICT DO NOTHING
            RETURNING user_id
        """.trimIndent()

        val insertedIds = databaseClient
            .sql(sql)
            .bind("eventId", eventId)
            .bind("userIds", ids)
            .map { row, _ -> UserId(row.required<UUID>("user_id")) }
            .all()
            .collectList()
            .awaitSingle()

        return insertedIds
    }

    // ==================== REMOVE ====================
    override suspend fun deleteParticipant(eventId: EventId, userId: UserId) {
        val sql = """
            DELETE FROM event_participants WHERE event_id = :eventId AND user_id = :userId
        """.trimIndent()

        val deleted = databaseClient
            .sql(sql)
            .bind("eventId", eventId.value)
            .bind("userId", userId.value)
            .map { _, _ -> 1 }
            .awaitSingleOrNull()

        if (deleted == null)
            throw ConflictException("Пользователь не является участником события")
    }

    override suspend fun deleteParticipants(
        eventId: EventId,
        userIds: List<UserId>
    ): Int {
        val ids = userIds.distinct().map { it.value }.toTypedArray()

        val sql = """
            DELETE FROM event_participants
            WHERE event_id = :eventId AND user_id IN (:userIds)
        """.trimIndent()

        val deleted = databaseClient
            .sql(sql)
            .bind("eventId", eventId.value)
            .bind("userIds", ids)
            .map { _, _ -> 1 }
            .awaitSingleOrNull()

        return deleted ?: 0
    }
}