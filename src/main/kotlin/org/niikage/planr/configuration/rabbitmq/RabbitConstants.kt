package org.niikage.planr.configuration.rabbitmq

import org.niikage.planr.features.users.query.UserView
import org.niikage.planr.shared.exceptions.BadRequestException

object RabbitConstants {
    private const val DAY_IN_MILLIS = 24L * 60 * 60 * 1000

    const val NOTIFICATION_EXCHANGE = "planr.notification.exchange"
    const val NOTIFICATION_TG_QUEUE = "notification.tg.queue"
    const val NOTIFICATION_VK_QUEUE = "notification.vk.queue"

    const val ROUTING_KEY_ALL = "notification.#.all"
    const val ROUTING_KEY_TG = "notification.#.tg"
    const val ROUTING_KEY_VK = "notification.#.vk"

    const val DLQ_EXCHANGE = "planr.dlq.exchange"
    const val DLQ_ROUTING_KEY = "dlq.routing.key"
    const val DLQ_QUEUE = "dlq.queue"

    // ==================== HEADERS ====================
    const val DLQ_EXCHANGE_HEADER = "x-dead-letter-exchange"
    const val DLQ_ROUTING_KEY_HEADER = "x-dead-letter-routing-key"
    const val TTL_HEADER = "x-message-ttl"
    const val TTL_MS = 3 * DAY_IN_MILLIS

    fun buildKey(user: UserView, part: String): String {
        return when {
            user.tgConnected && user.vkConnected -> buildKey(ROUTING_KEY_ALL, part)
            user.tgConnected -> buildKey(ROUTING_KEY_TG, part)
            user.vkConnected -> buildKey(ROUTING_KEY_VK, part)
            else -> throw BadRequestException("У пользователя должна быть подключена хотя бы одна социальная сеть")
        }
    }

    private fun buildKey(key: String, part: String): String {
        return key.replace("#", part)
    }
}