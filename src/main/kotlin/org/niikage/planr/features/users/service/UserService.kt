package org.niikage.planr.features.users.service

import org.niikage.planr.features.users.domain.UserDomain
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.features.users.domain.UserSocials
import org.niikage.planr.features.users.dto.UserCreateRequest
import org.niikage.planr.features.users.dto.UserUpdateRequest

interface UserService {
    suspend fun getUser(id: UserId): UserDomain
    suspend fun getUser(socials: UserSocials): UserDomain
    suspend fun getUsers(userIds: List<UserId>): List<UserDomain>

    suspend fun create(request: UserCreateRequest): UserDomain
    suspend fun update(id: UserId, request: UserUpdateRequest): UserDomain

    suspend fun delete(id: UserId)
}