package org.niikage.planr.features.recentcontacts.service

import org.niikage.planr.features.recentcontacts.domain.RecentContactsList
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.shared.kernel.PageRequest

interface RecentContactService {
    suspend fun findAllByOwnerId(ownerId: UserId, pageRequest: PageRequest): RecentContactsList

    suspend fun addOrRefresh(ownerId: UserId, userId: UserId)

    suspend fun delete(ownerId: UserId, userId: UserId)

    suspend fun deleteExpiredByOwnerId(ownerId: UserId)

    suspend fun deleteAllByOwnerId(ownerId: UserId)
}