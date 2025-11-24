package com.tedioinfernal.tediosession.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tedioinfernal.tediosession.annotation.EventHandler;
import com.tedioinfernal.tediosession.dto.MessageDTO;
import com.tedioinfernal.tediosession.dto.WhatsAppMessageDTO;
import com.tedioinfernal.tediosession.entity.EvolutionInstance;
import com.tedioinfernal.tediosession.entity.EvolutionSession;
import com.tedioinfernal.tediosession.entity.MessageSession;
import com.tedioinfernal.tediosession.producer.MessageProducer;
import com.tedioinfernal.tediosession.service.EventHandlerService;
import com.tedioinfernal.tediosession.service.MessageSessionService;
import com.tedioinfernal.tediosession.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@EventHandler("MESSAGE-RECEIVED")
@RequiredArgsConstructor
public class MessageReceivedEventHandler implements EventHandlerService {

    private final SessionService sessionService;
    private final MessageSessionService messageSessionService;
    private final MessageProducer messageProducer;
    private final ObjectMapper objectMapper;

    @Override
    @Async
    public void handle(Map<String, Object> object) {
        log.info("Processing MESSAGE-RECEIVED event");
        log.debug("Received object: {}", object);
        
        try {
            // Converter o Map para DTO
            WhatsAppMessageDTO messageData = objectMapper.convertValue(object, WhatsAppMessageDTO.class);
            
            processWhatsAppMessage(messageData, object);
            
            log.info("MESSAGE-RECEIVED event processed successfully");
        } catch (Exception e) {
            log.error("Error processing MESSAGE-RECEIVED event", e);
            throw new RuntimeException("Failed to process MESSAGE-RECEIVED event", e);
        }
    }
    
    private void processWhatsAppMessage(WhatsAppMessageDTO messageData, Map<String, Object> originalObject) {
        log.info("Processing WhatsApp message from instance: {}", messageData.getInstance());
        
        // Buscar a instância pelo nome
        Optional<EvolutionInstance> instanceOpt = sessionService.findInstanceByName(messageData.getInstance());
        
        if (instanceOpt.isEmpty()) {
            log.warn("Instance not found: {}", messageData.getInstance());
            return;
        }
        
        EvolutionInstance instance = instanceOpt.get();
        log.info("Instance found: {} (ID: {})", instance.getInstanceName(), instance.getId());
        log.info("Evolution: {} (URL: {})", instance.getEvolution().getNome(), instance.getEvolution().getUrl());
        
        // Extrair remoteJid
        String remoteJid = messageData.getData().getKey().getRemoteJid();
        log.info("Remote JID: {}", remoteJid);
        
        // Buscar ou criar sessão
        EvolutionSession session = sessionService.findOrCreateSession(instance, remoteJid);
        log.info("Session ID: {}, Status: {}", session.getId(), session.getStatus());
        
        // Buscar última mensagem da sessão para verificar status
        Optional<MessageSession> lastMessageOpt = messageSessionService.findLatestByRemoteJid(remoteJid);
        
        String currentEvent = "MESSAGE-RECEIVED";
        if (lastMessageOpt.isPresent()) {
            MessageSession lastMessage = lastMessageOpt.get();
            log.info("Last message status: {}", lastMessage.getCurrentEvent());
            
            // Se última mensagem foi processada, usar esse status para nova mensagem
            if ("MESSAGE-PROCESSED".equals(lastMessage.getCurrentEvent())) {
                currentEvent = "MESSAGE-PROCESSED";
                log.info("Using MESSAGE-PROCESSED status for new message");
            } else if ("TASK-DETAIL-REQUESTED".equals(lastMessage.getCurrentEvent())) {
                currentEvent = "TASK-DETAIL-REQUESTED";
                log.info("Using TASK-DETAIL-REQUESTED status for new message");
            }
        }
        
        // Salvar mensagem na tabela message_session
        MessageSession messageSession = messageSessionService.saveMessageSession(
                session,
                originalObject,
                currentEvent,
                messageData.getData().getMessageTimestamp(),
                remoteJid,
                messageData.getData().getKey().getFromMe(),
                messageData.getData().getMessageType(),
                messageData.getData().getPushName()
        );
        
        log.info("Message session saved with ID: {} and status: {}", messageSession.getId(), currentEvent);
        
        // Publicar mensagem na fila com evento MESSAGE-PROCESSOR
        publishToProcessor(messageSession, originalObject);
        
        // Processar a mensagem
        processMessageContent(messageData, instance, session);
    }
    
    private void publishToProcessor(MessageSession messageSession, Map<String, Object> originalObject) {
        try {
            log.info("Publishing message to MESSAGE-PROCESSOR event");
            
            Map<String, Object> processorObject = new HashMap<>(originalObject);
            processorObject.put("messageSessionId", messageSession.getId());
            
            MessageDTO processorMessage = MessageDTO.builder()
                    .event("MESSAGE-PROCESSOR")
                    .object(processorObject)
                    .build();
            
            messageProducer.sendMessage(processorMessage);
            
            log.info("Message published to MESSAGE-PROCESSOR successfully");
        } catch (Exception e) {
            log.error("Error publishing message to MESSAGE-PROCESSOR", e);
        }
    }
    
    private void processMessageContent(WhatsAppMessageDTO messageData, 
                                       EvolutionInstance instance, 
                                       EvolutionSession session) {
        log.info("Processing message content");
        log.info("Message Type: {}", messageData.getData().getMessageType());
        log.info("Push Name: {}", messageData.getData().getPushName());
        log.info("Message: {}", messageData.getData().getMessage());
        log.info("From Me: {}", messageData.getData().getKey().getFromMe());
        log.info("Status: {}", messageData.getData().getStatus());
        
        // Aqui você pode implementar a lógica específica de processamento
        // Por exemplo: salvar mensagem, enviar resposta automática, etc.
        
        log.debug("Session details - ID: {}, RemoteJid: {}, Status: {}", 
                 session.getId(), session.getRemoteJid(), session.getStatus());
    }
}
