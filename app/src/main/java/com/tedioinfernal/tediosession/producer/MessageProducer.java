package com.tedioinfernal.tediosession.producer;

import com.tedioinfernal.tediosession.dto.MessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    public void sendMessage(MessageDTO message) {
        try {
            log.info("Sending message to exchange: {} with routing key: {}", exchangeName, routingKey);
            log.debug("Message content: {}", message);
            
            rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
            
            log.info("Message sent successfully");
        } catch (Exception e) {
            log.error("Error sending message to RabbitMQ", e);
            throw new RuntimeException("Failed to send message", e);
        }
    }
}
