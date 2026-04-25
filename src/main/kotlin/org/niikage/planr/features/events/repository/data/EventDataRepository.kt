package org.niikage.planr.features.events.repository.data

import kotlinx.coroutines.flow.Flow
import org.niikage.planr.features.events.entity.EventEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface EventDataRepository : CoroutineCrudRepository<EventEntity, UUID> {

    @Query(
        """
            SELECT 
                id,
                creator_id,
                type,
                title,
                description,
                location,
                start_time,
                end_time,
                created_at,
                version
            FROM events WHERE creator_id = :creatorId
            LIMIT :limit OFFSET :offset
        """
    )
    fun findAllByCreatorId(creatorId: UUID, limit: Int, offset: Int): Flow<EventEntity>
}