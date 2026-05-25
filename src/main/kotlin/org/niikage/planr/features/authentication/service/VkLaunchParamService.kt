package org.niikage.planr.features.authentication.service


import org.niikage.planr.features.authentication.dto.VkUserDto
import org.niikage.planr.shared.exceptions.BadRequestException
import org.niikage.planr.shared.exceptions.UnauthorizedException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class VkLaunchParamsService(
    @Value("\${vk.app.client-secret}") clientSecret: String
) {
    private val PARAMS_EXPIRATION_SECONDS = 3600L
    private val HMAC_SHA256 = "HmacSHA256"

    private val macThreadLocal: ThreadLocal<Mac> = ThreadLocal.withInitial {
        Mac.getInstance(HMAC_SHA256).also {
            it.init(SecretKeySpec(clientSecret.toByteArray(StandardCharsets.UTF_8), HMAC_SHA256))
        }
    }

    fun validateAndParseLaunchParams(launchParams: String): VkUserDto {
        val clean = if (launchParams.startsWith("?")) launchParams.drop(1) else launchParams

        val params = clean.split("&").associate { entry ->
            val parts = entry.split("=", limit = 2)
            parts[0] to (parts.getOrNull(1)
                ?.let { URLDecoder.decode(it, StandardCharsets.UTF_8) }
                ?: "")
        }

        val receivedSign = params["vk_sign"]
            ?: throw BadRequestException("Отсутствует vk_sign в параметрах запуска")

        val queryForCheck = params.entries
            .filter { it.key.startsWith("vk_") && it.key != "vk_sign" }
            .sortedBy { it.key }
            .joinToString("&") { "${it.key}=${it.value}" }

        val mac = macThreadLocal.get()
        mac.reset()
        val rawHmac = mac.doFinal(queryForCheck.toByteArray(StandardCharsets.UTF_8))

        val calculatedSign = Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac)

        if (calculatedSign != receivedSign) {
            throw UnauthorizedException("Недействительная подпись параметров запуска ВК")
        }

        val vkTs = params["vk_ts"]?.toLongOrNull()
            ?: throw BadRequestException("Отсутствует vk_ts в параметрах запуска")

        if (Instant.now().epochSecond - vkTs > PARAMS_EXPIRATION_SECONDS) {
            throw UnauthorizedException("Параметры запуска ВК устарели, войдите снова")
        }

        return VkUserDto(
            vkUserId = params["vk_user_id"]
                ?: throw BadRequestException("Отсутствует vk_user_id в параметрах запуска"),
            language = params["vk_language"],
            platform = params["vk_platform"]
        )
    }
}