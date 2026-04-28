package org.niikage.planr.features.events.domain

import org.niikage.planr.features.events.entity.EventEntity
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.shared.exceptions.BadRequestException
import java.time.OffsetDateTime

enum class EventType {
    OPTIONAL
}

data class EventDomain(
    val id: EventId,
    val creatorId: UserId,
    val title: String,
    val type: EventType,
    val description: String?,
    val location: String,
    val startTime: OffsetDateTime,
    val endTime: OffsetDateTime?,
    val createdAt: OffsetDateTime,

    internal val version: Long? = null
) {

    init {
        validate()
    }

    fun validate() {
        if (startTime.isBefore(OffsetDateTime.now())) {
            throw BadRequestException("Событие не может начинаться в прошлом")
        }

        if (endTime != null && endTime.isBefore(startTime)) {
            throw BadRequestException("Событие не может закончиться раньше начала")
        }
    }
}

fun EventEntity.toDomain(): EventDomain {
    return EventDomain(
        id = this.id.toEventId(),
        creatorId = UserId(this.creatorId),
        title = this.title,
        type = this.type,
        description = this.description,
        location = this.location,
        startTime = this.startTime,
        endTime = this.endTime,
        createdAt = this.createdAt,
        version = this.version
    )
}

fun EventDomain.toEntity(): EventEntity {
    return EventEntity(
        id = this.id.value,
        creatorId = this.creatorId.value,
        title = this.title,
        type = this.type,
        description = this.description,
        location = this.location,
        startTime = this.startTime,
        endTime = this.endTime,
        createdAt = this.createdAt,
        version = this.version
    )
}