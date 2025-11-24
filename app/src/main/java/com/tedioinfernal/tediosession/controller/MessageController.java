package com.tedioinfernal.tediosession.controller;

import com.tedioinfernal.tediosession.dto.MessageDTO;
import com.tedioinfernal.tediosession.producer.MessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageProducer messageProducer;

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody MessageDTO messageDTO) {
        try {
            log.info("Received request to send message: {}", messageDTO);
            messageProducer.sendMessage(messageDTO);
            return ResponseEntity.ok("Message sent successfully");
        } catch (Exception e) {
            log.error("Error sending message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error sending message: " + e.getMessage());
        }
    }
}
