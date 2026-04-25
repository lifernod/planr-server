package org.niikage.planr.features.recentcontacts.domain

import org.niikage.planr.features.users.domain.UserId

data class RecentContactsList(
    val ownerId: UserId,
    val contacts: Map<UserId, RecentContact>
)
