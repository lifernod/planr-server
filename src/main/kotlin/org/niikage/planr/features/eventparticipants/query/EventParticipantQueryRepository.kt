package org.niikage.planr.features.eventparticipants.query

import kotlinx.coroutines.reactor.awaitSingle
import org.niikage.planr.features.eventparticipants.query.entity.EventParticipantRole
import org.niikage.planr.features.eventparticipants.query.entity.EventParticipantsList
import org.niikage.planr.features.eventparticipants.query.entity.mapEventParticipant
import org.niikage.planr.features.users.query.UserQueryRepository
import org.niikage.planr.shared.exceptions.ConflictException
import org.niikage.planr.shared.kernel.PageRequest
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitSingleOrNull
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class EventParticipantQueryRepository(
    private val databaseClient: DatabaseClient,
    private val userQueryRepository: UserQueryRepository
) {
    suspend fun findAllByEventId(
        eventId: UUID,
        pageRequest: PageRequest,
        role: EventParticipantRole = EventParticipantRole.PARTICIPANT
    ): EventParticipantsList {
        val sql = """
            SELECT 
                event_id,
                user_id AS participant_id,
                role AS participant_role
            FROM event_participants
            WHERE event_id = :eventId AND role = :role
        """.trimIndent()

        val participants = databaseClient
            .sql(sql)
            .bind("eventId", eventId)
            .bind("role", role.name)
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

    suspend fun addParticipant(
        eventId: UUID,
        userId: UUID,
        role: EventParticipantRole = EventParticipantRole.PARTICIPANT
    ) {
        val sql = """
            INSERT INTO event_participants(event_id, user_id, role)
            VALUES (:eventId, :userId, :role)
            ON CONFLICT DO NOTHING
            RETURNING 1
        """.trimIndent()

        val inserted = databaseClient
            .sql(sql)
            .bind("eventId", eventId)
            .bind("userId", userId)
            .bind("role", role.name)
            .map { _, _ -> 1 }
            .awaitSingleOrNull()

        if (inserted == null)
            throw ConflictException("Пользователь уже является участником события")
    }
}