package org.niikage.planr.features.invitations.domain

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.niikage.planr.features.users.domain.UserDomain
import org.niikage.planr.features.users.query.UserView
import java.time.OffsetDateTime
import java.util.UUID

enum class InvitationResponseStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "__type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = UnnamedInvitation::class, name = "unnamed"),
    JsonSubTypes.Type(value = NamedInvitation::class, name = "named")
)
sealed interface Invitation {
    val invitationId: UUID
    val taget: InvitationTarget
    val sender: UserView
    val createdAt: OffsetDateTime
}

data class UnnamedInvitation(
    override val invitationId: UUID = UUID.randomUUID(),
    override val taget: InvitationTarget,
    override val sender: UserView,
    override val createdAt: OffsetDateTime = OffsetDateTime.now()
) : Invitation

data class NamedInvitation(
    override val invitationId: UUID = UUID.randomUUID(),
    override val taget: InvitationTarget,
    override val sender: UserView,
    override val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val receiver: UserDomain,
    var responseStatus: InvitationResponseStatus = InvitationResponseStatus.PENDING,
    var respondedAt: OffsetDateTime? = null,
    val routingKey: String
) : Invitation
