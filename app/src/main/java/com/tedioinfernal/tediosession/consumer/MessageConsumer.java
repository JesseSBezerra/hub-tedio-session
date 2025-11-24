package com.tedioinfernal.tediosession.consumer;

import com.tedioinfernal.tediosession.dto.MessageDTO;
import com.tedioinfernal.tediosession.service.EventDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageConsumer {

    private final EventDispatcher eventDispatcher;

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void consumeMessage(MessageDTO message) {
        try {
            log.info("Message received from queue: {}", message);
            eventDispatcher.dispatch(message);
        } catch (Exception e) {
            log.error("Error consuming message", e);
            throw e;
        }
    }
}
