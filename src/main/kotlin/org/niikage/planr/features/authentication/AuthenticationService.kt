package org.niikage.planr.features.authentication

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.niikage.planr.features.users.domain.UserDomain
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.features.users.domain.UserRole
import org.niikage.planr.features.users.domain.UserSocials
import org.niikage.planr.features.users.dto.UserCreateRequest
import org.niikage.planr.features.users.service.UserService
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey
import kotlin.system.exitProcess

@Service
class AuthenticationService(
    private val environment: Environment,
    private val userService: UserService,
) {
    private val secret: SecretKey

    init {
        val key = environment.getProperty("security.jwt")

        if (key == null) {
            throw RuntimeException("Не установлено значение секретного ключа авторизации")
            exitProcess(1)
        }

        secret = Keys.hmacShaKeyFor(
            Decoders.BASE64.decode(key)
        )
    }

    fun generateToken(user: UserDomain): String {
        return Jwts.builder()
            .subject(user.id.value.toString())
            .claim("role", user.role.name)
            .claim("tgId", user.socials.tgId)
            .claim("vkId", user.socials.vkId)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
            .signWith(secret)
            .compact()
    }

    fun extractUser(token: String): SecurityUserClaims {
        val parser = Jwts.parser().verifyWith(secret).build()

        val payload = parser.parseSignedClaims(token).payload
        return SecurityUserClaims(
            id = UserId(UUID.fromString(payload.subject)),
            role = UserRole.valueOf(payload.get("role", String::class.java)),
            tgId = payload.get("tgId", String::class.java),
            vkId = payload.get("vkId", String::class.java)
        )
    }

    suspend fun signInWithTg(tgId: String): String {
        val user = userService.getUser(UserSocials.ofTg(tgId = tgId))
        return generateToken(user)
    }

    suspend fun signInWithVk(vkId: String): String {
        val user = userService.getUser(UserSocials.ofVk(vkId = vkId))
        return generateToken(user)
    }

    suspend fun signUp(request: UserCreateRequest): String {
        val createdUser = userService.create(request)
        return generateToken(createdUser)
    }
}