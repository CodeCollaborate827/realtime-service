package com.chat.realtime_service.config;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    private static final boolean EXCHANGE_AUTO_DELETE = false; // Don't delete the exchange when the last queue is unbound from it

    @Value("${rabbitmq.message.exchange.name}")
    private String topicExchangeName;
    @Value("${rabbitmq.message.exchange.durable}")
    private boolean exchangeDurable;

    private AmqpAdmin amqpAdmin;

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(topicExchangeName, exchangeDurable, EXCHANGE_AUTO_DELETE);
    }

}
