package org.niikage.planr.features.recentcontacts.service

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.niikage.planr.features.recentcontacts.domain.RecentContact
import org.niikage.planr.features.recentcontacts.domain.RecentContactsList
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.features.users.domain.toUserId
import org.niikage.planr.features.users.query.UserQueryRepository
import org.niikage.planr.shared.kernel.PageRequest
import org.springframework.data.domain.Range
import org.springframework.data.redis.connection.Limit
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Service
class RecentContactService(
    private val redisTemplate: ReactiveRedisTemplate<String, String>,
    private val userQueryRepository: UserQueryRepository
) {
    companion object {
        private val TTL = Duration.ofDays(7)
    }

    suspend fun findAllByOwnerId(
        ownerId: UserId,
        pageRequest: PageRequest
    ): RecentContactsList {
        val primaryKey = primaryKey(ownerId)
        val countKey = countKey(ownerId)

        val tuples = redisTemplate
            .opsForZSet()
            .rangeByScoreWithScores(
                primaryKey,
                Range.unbounded(),
                Limit.limit().count(pageRequest.limit).offset(pageRequest.offset)
            )
            .collectList()
            .awaitSingleOrNull() ?: emptyList()

        if (tuples.isEmpty())
            return RecentContactsList(ownerId = ownerId, contacts = emptyMap())

        val counts = redisTemplate
            .opsForHash<String, String>()
            .entries(countKey)
            .collectMap(
                { UUID.fromString(it.key) },
                { it.value.toInt() },
            )
            .awaitSingle()

        val users = userQueryRepository.findAllByIds(
            tuples.map { UUID.fromString(it.value) },
        )

        val contacts = tuples
            .associate { tuple ->
                val userId = UUID.fromString(tuple.value)
                val timestamp = tuple.score!!.toLong()

                userId.toUserId() to RecentContact(
                    userId = userId.toUserId(),
                    user = users[userId]!!,
                    lastInteractionAt = OffsetDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneOffset.UTC),
                    interactionCount = counts[userId] ?: 1
                )
            }

        return RecentContactsList(ownerId, contacts)
    }

    // ==================== ADD OR REFRESH ====================
    suspend fun addOrRefresh(
        ownerId: UserId,
        userId: UserId
    ) {
        val primaryKey = primaryKey(ownerId)
        val countKey = countKey(ownerId)
        val now = OffsetDateTime.now().toEpochSecond().toDouble()

        redisTemplate
            .opsForZSet()
            .add(primaryKey, userId.value.toString(), now)
            .awaitSingle()

        redisTemplate
            .opsForHash<String, String>()
            .increment(countKey, userId.value.toString(), 1)
            .awaitSingle()

        redisTemplate.expire(primaryKey, TTL).awaitSingle()
        redisTemplate.expire(countKey, TTL).awaitSingle()
    }

    // ==================== DELETE ====================
    suspend fun delete(
        ownerId: UserId,
        userId: UserId
    ) {
        redisTemplate
            .opsForZSet()
            .remove(primaryKey(ownerId), userId.value.toString())
            .awaitSingle()

        redisTemplate
            .opsForHash<String, String>()
            .remove(countKey(ownerId), userId.value.toString())
            .awaitSingle()
    }

    suspend fun deleteExpiredByOwnerId(ownerId: UserId) {
        val primaryKey = primaryKey(ownerId)
        val countKey = countKey(ownerId)

        val contacts = redisTemplate
            .opsForZSet()
            .rangeWithScores(primaryKey, Range.unbounded())
            .collectList()
            .awaitSingle()

        val expired = contacts.filter {
            val timestamp = it.score!!.toLong()
            val last = Instant.ofEpochSecond(timestamp)
            Duration.between(last, Instant.now()).toDays() > 7
        }

        expired.forEach {
            val userId = it.value

            redisTemplate
                .opsForZSet()
                .remove(primaryKey, userId!!)
                .awaitSingle()

            redisTemplate
                .opsForHash<String, String>()
                .remove(countKey, userId)
                .awaitSingle()
        }
    }

    suspend fun deleteAllByOwnerId(ownerId: UserId) {
        redisTemplate
            .delete(primaryKey(ownerId), countKey(ownerId))
            .awaitSingle()
    }

    // ==================== HELPERS ====================
    private fun primaryKey(ownerId: UserId): String = "recent:${ownerId.value}"
    private fun countKey(ownerId: UserId): String = "recent:count:${ownerId.value}"
}