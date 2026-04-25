package org.niikage.planr.features.events.entity

import org.niikage.planr.features.events.domain.EventType
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime
import java.util.*

@Table(name = "events")
data class EventEntity(
    @Id
    val id: UUID,
    @Column("creator_id")
    val creatorId: UUID,
    val title: String,
    val type: EventType,
    val description: String?,
    val location: String,
    @Column("start_time")
    val startTime: OffsetDateTime,
    @Column("end_time")
    val endTime: OffsetDateTime?,
    @Column("created_at")
    val createdAt: OffsetDateTime,

    @Version
    val version: Long? = null,
)
