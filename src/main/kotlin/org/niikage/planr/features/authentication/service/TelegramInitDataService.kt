package org.niikage.planr.features.authentication.service

import org.niikage.planr.features.authentication.dto.TelegramUserDto
import org.niikage.planr.shared.exceptions.BadRequestException
import org.niikage.planr.shared.exceptions.UnauthorizedException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class TelegramInitDataService(
    @Value("\${telegram.bot.token}") private val botToken: String,
    private val objectMapper: ObjectMapper
) {
    private val INIT_DATA_EXPIRATION_SECONDS = 86400L

    private val precomputedSecretKey: ByteArray by lazy {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec("WebAppData".toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
        mac.doFinal(botToken.toByteArray(StandardCharsets.UTF_8))
    }

    fun validateAndParseInitData(initData: String): TelegramUserDto {
        val decoded = URLDecoder.decode(initData, StandardCharsets.UTF_8)
        val params = decoded.split("&").associate {
            val parts = it.split("=", limit = 2)
            parts[0] to (parts.getOrNull(1) ?: "")
        }

        val receivedHash = params["hash"]
            ?: throw BadRequestException("Отсутствует hash в initData")

        val dataCheckString = params.entries
            .filter { it.key != "hash" }
            .sortedBy { it.key }
            .joinToString("\n") { "${it.key}=${it.value}" }

        val hashMac = Mac.getInstance("HmacSHA256")
        hashMac.init(SecretKeySpec(precomputedSecretKey, "HmacSHA256"))
        val calculatedHash = hashMac
            .doFinal(dataCheckString.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

        if (calculatedHash != receivedHash) {
            throw UnauthorizedException("Недействительная подпись Telegram initData")
        }

        val authDate = params["auth_date"]?.toLongOrNull()
            ?: throw BadRequestException("Отсутствует auth_date в initData")

        if (Instant.now().epochSecond - authDate > INIT_DATA_EXPIRATION_SECONDS) {
            throw UnauthorizedException("Telegram initData устарел, войдите снова")
        }

        val userJson = params["user"]
            ?: throw BadRequestException("Отсутствует user в initData")

        return objectMapper.readValue(userJson, TelegramUserDto::class.java)
    }
}