package org.niikage.planr.configuration.rabbitmq

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class RabbitConfiguration {
    // ==================== EXCHANGES ====================
    @Bean
    fun notificationExchange() =
        TopicExchange(RabbitConstants.NOTIFICATION_EXCHANGE)

    @Bean
    fun dlqExchange() =
        DirectExchange(RabbitConstants.DLQ_EXCHANGE)

    // ==================== QUEUES ====================
    @Bean
    fun tgQueue() =
        createDefaultQueue(RabbitConstants.NOTIFICATION_TG_QUEUE)

    @Bean
    fun vkQueue() =
        createDefaultQueue(RabbitConstants.NOTIFICATION_VK_QUEUE)

    @Bean
    fun dlqQueue() =
        createDefaultQueue(RabbitConstants.DLQ_QUEUE)

    // ==================== BINDINGS ====================
    @Bean
    fun tgNotificationBinding() =
        bindNotificationQueue(RabbitConstants.ROUTING_KEY_TG, ::tgQueue)

    @Bean
    fun tgNotificationAllBinding() =
        bindNotificationQueue(RabbitConstants.ROUTING_KEY_ALL, ::tgQueue)

    @Bean
    fun vkNotificationBinding() =
        bindNotificationQueue(RabbitConstants.ROUTING_KEY_VK, ::vkQueue)

    @Bean
    fun vkNotificationAllBinding() =
        bindNotificationQueue(RabbitConstants.ROUTING_KEY_ALL, ::vkQueue)

    @Bean
    fun dlqBinding(): Binding {
        return BindingBuilder
            .bind(dlqQueue())
            .to(dlqExchange())
            .with(RabbitConstants.DLQ_ROUTING_KEY)
    }

    // ==================== TEMPLATE ====================
    @Bean
    fun jsonMessageConverter() =
        JacksonJsonMessageConverter()

    @Bean
    fun rabbitConnectionFactory(env: Environment): ConnectionFactory {
        val factory = CachingConnectionFactory()
        factory.username = env.getRequiredProperty("spring.rabbitmq.username")
        factory.setPassword(env.getRequiredProperty("spring.rabbitmq.password"))
        factory.setHost(env.getRequiredProperty("spring.rabbitmq.host"))
        factory.virtualHost = env.getRequiredProperty("spring.rabbitmq.virtual-host")
        return factory
    }

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val template = RabbitTemplate()
        template.connectionFactory = connectionFactory
        template.messageConverter = jsonMessageConverter()
        template.setMandatory(true)

        return template
    }

    // ==================== HELPERS ====================
    private fun createDefaultQueue(name: String): Queue {
        return QueueBuilder
            .durable(name)
            .withArgument(RabbitConstants.DLQ_EXCHANGE_HEADER, RabbitConstants.DLQ_EXCHANGE)
            .withArgument(RabbitConstants.DLQ_ROUTING_KEY_HEADER, RabbitConstants.DLQ_ROUTING_KEY)
            .withArgument(RabbitConstants.TTL_HEADER, RabbitConstants.TTL_MS)
            .build()
    }

    private fun bindNotificationQueue(routingKey: String, queue: () -> Queue): Binding {
        return BindingBuilder
            .bind(queue())
            .to(notificationExchange())
            .with(routingKey)
    }
}