package com.chat.realtime_service.service;

import com.chat.realtime_service.models.WebsocketMessage;
import com.chat.realtime_service.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AmqpMessageSender {

    @Value("${rabbitmq.message.exchange.name}")
    private String messageExchangeName;

    private final RabbitTemplate rabbitTemplate;


    public void sendMessageToUser(String userId, WebsocketMessage message) {
        String json = JsonUtils.toJson(message);
        Message amqpMessage = new Message(json.getBytes());
        log.info("Sending message to user: {}, AMQP topic: {}", userId, messageExchangeName);
        rabbitTemplate.send(messageExchangeName, userId, amqpMessage);
    }
}
