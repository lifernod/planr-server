package org.niikage.planr.features.recentcontacts.domain

import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.features.users.query.UserView
import java.time.OffsetDateTime

data class RecentContact(
    val userId: UserId,
    var user: UserView? = null,

    val lastInteractionAt: OffsetDateTime = OffsetDateTime.now(),
    val interactionCount: Int = 0,
)