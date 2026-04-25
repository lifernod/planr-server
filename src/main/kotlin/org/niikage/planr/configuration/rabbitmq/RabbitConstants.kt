package org.niikage.planr.configuration.rabbitmq

object RabbitConstants {
    private const val DAY_IN_MILLIS = 24L * 60 * 60 * 1000

    const val NOTIFICATION_EXCHANGE = "planr.notification.exchange"
    const val NOTIFICATION_TG_QUEUE = "notification.tg.queue"
    const val NOTIFICATION_VK_QUEUE = "notification.vk.queue"

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

    fun buildTgKey(part: String = "message"): String =
        ROUTING_KEY_TG.replace("#", part)

    fun buildVkKey(part: String = "message"): String =
        ROUTING_KEY_VK.replace("#", part)
}