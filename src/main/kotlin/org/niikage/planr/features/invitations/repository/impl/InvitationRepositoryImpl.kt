package org.niikage.planr.features.invitations.repository.impl

import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.niikage.planr.features.invitations.domain.Invitation
import org.niikage.planr.features.invitations.repository.InvitationRepository
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration
import java.util.*

@Repository
class InvitationRepositoryImpl(
    private val redisTemplate: ReactiveRedisTemplate<String, Invitation>
) : InvitationRepository {
    private val ttl = Duration.ofDays(14)

    private fun key(invitationId: UUID) = "invitations:$invitationId"

    override suspend fun findInvitationById(invitationId: UUID): Invitation? {
        return redisTemplate
            .opsForValue()
            .get(key(invitationId))
            .awaitSingleOrNull()
    }

    override suspend fun saveInvitation(invitation: Invitation) {
        redisTemplate
            .opsForValue()
            .set(key(invitation.invitationId), invitation)
            .awaitSingle()

        redisTemplate.expire(key(invitation.invitationId), ttl)
    }
}