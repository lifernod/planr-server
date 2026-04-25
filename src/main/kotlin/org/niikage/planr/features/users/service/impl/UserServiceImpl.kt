package org.niikage.planr.features.users.service.impl

import org.niikage.planr.features.users.domain.UserDomain
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.features.users.domain.UserRole
import org.niikage.planr.features.users.domain.UserSocials
import org.niikage.planr.features.users.dto.UserCreateRequest
import org.niikage.planr.features.users.dto.UserUpdateRequest
import org.niikage.planr.features.users.repository.UserRepository
import org.niikage.planr.features.users.service.UserService
import org.niikage.planr.shared.exceptions.BadRequestException
import org.niikage.planr.shared.exceptions.maybeNotFound
import org.niikage.planr.shared.exceptions.maybeViolation
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class UserServiceImpl(
    private val repo: UserRepository
) : UserService {
    // ==================== GET ====================
    override suspend fun getUser(id: UserId): UserDomain {
        return maybeNotFound("Пользователь не найден") {
            repo.findById(id)
        }
    }

    override suspend fun getUser(socials: UserSocials): UserDomain {
        return maybeNotFound("Пользователь не найден") {
            when {
                socials.isTgConnected() -> repo.findByTgId(socials.tgId!!)
                socials.isVkConnected() -> repo.findByVkId(socials.vkId!!)
                else -> null
            }
        }
    }

    // ==================== CREATE ====================
    override suspend fun create(request: UserCreateRequest): UserDomain {
        if (!request.socials.isSocialConnected())
            throw BadRequestException("Хотя бы одна из социальных сетей должна быть указана")

        val user = UserDomain(
            id = UserId.random(),
            name = request.name,
            role = UserRole.USER,
            socials = request.socials,
            createdAt = OffsetDateTime.now()
        )

        return maybeViolation("Пользователь уже существует") {
            repo.createUser(user)
        }
    }

    // ==================== UPDATE ====================
    override suspend fun update(id: UserId, request: UserUpdateRequest): UserDomain {
        val user = getUser(id)

        val updatedUser = user.copy(
            name = request.name ?: user.name,
            socials = request.socials ?: user.socials,
        )

        return maybeViolation("Пользователь уже существует") {
            repo.updateUser(updatedUser)
        }
    }

    // ==================== DELETE ====================
    override suspend fun delete(id: UserId) {
        repo.deleteById(id)
    }
}