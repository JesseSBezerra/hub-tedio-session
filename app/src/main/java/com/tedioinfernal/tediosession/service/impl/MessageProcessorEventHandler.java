package com.tedioinfernal.tediosession.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tedioinfernal.tediosession.annotation.EventHandler;
import com.tedioinfernal.tediosession.dto.MessageDTO;
import com.tedioinfernal.tediosession.dto.WhatsAppMessageDTO;
import com.tedioinfernal.tediosession.entity.MessageSession;
import com.tedioinfernal.tediosession.producer.MessageProducer;
import com.tedioinfernal.tediosession.service.EventHandlerService;
import com.tedioinfernal.tediosession.service.EvolutionApiService;
import com.tedioinfernal.tediosession.service.MessageSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@EventHandler("MESSAGE-PROCESSOR")
@RequiredArgsConstructor
public class MessageProcessorEventHandler implements EventHandlerService {

    private final MessageSessionService messageSessionService;
    private final EvolutionApiService evolutionApiService;
    private final MessageProducer messageProducer;
    private final ObjectMapper objectMapper;

    @Override
    @Async
    public void handle(Map<String, Object> object) {
        log.info("Processing MESSAGE-PROCESSOR event");
        
        try {
            // Extrair messageSessionId
            Long messageSessionId = extractMessageSessionId(object);
            
            if (messageSessionId == null) {
                log.error("messageSessionId not found in object");
                return;
            }
            
            // Buscar MessageSession
            Optional<MessageSession> messageSessionOpt = messageSessionService.findById(messageSessionId);
            
            if (messageSessionOpt.isEmpty()) {
                log.error("MessageSession not found with ID: {}", messageSessionId);
                return;
            }
            
            MessageSession messageSession = messageSessionOpt.get();
            log.info("Processing MessageSession ID: {}, Current Event: {}", 
                     messageSession.getId(), messageSession.getCurrentEvent());
            
            // Verificar se o evento atual é MESSAGE-RECEIVED
            if ("MESSAGE-RECEIVED".equals(messageSession.getCurrentEvent())) {
                processMessageReceived(messageSession, object);
            } else if ("MESSAGE-PROCESSED".equals(messageSession.getCurrentEvent())) {
                // Verificar última mensagem do usuário para processar opções do menu
                processUserResponse(messageSession, object);
            } else if ("TASK-DETAIL-REQUESTED".equals(messageSession.getCurrentEvent())) {
                // Usuário enviou detalhamento da tarefa
                processTaskDetailResponse(messageSession, object);
            } else {
                log.warn("Current event not handled: {}", messageSession.getCurrentEvent());
            }
            
            log.info("MESSAGE-PROCESSOR event processed successfully");
            
        } catch (Exception e) {
            log.error("Error processing MESSAGE-PROCESSOR event", e);
            throw new RuntimeException("Failed to process MESSAGE-PROCESSOR event", e);
        }
    }
    
    private Long extractMessageSessionId(Map<String, Object> object) {
        Object idObj = object.get("messageSessionId");
        if (idObj instanceof Number) {
            return ((Number) idObj).longValue();
        }
        return null;
    }
    
    private void processMessageReceived(MessageSession messageSession, Map<String, Object> object) {
        try {
            log.info("Processing MESSAGE-RECEIVED for MessageSession ID: {}", messageSession.getId());
            
            // Converter objeto para WhatsAppMessageDTO
            WhatsAppMessageDTO messageData = objectMapper.convertValue(object, WhatsAppMessageDTO.class);
            
            // Extrair número do remoteJid (remover @s.whatsapp.net)
            String remoteJid = messageData.getData().getKey().getRemoteJid();
            String number = extractPhoneNumber(remoteJid);
            
            log.info("Extracted phone number: {} from remoteJid: {}", number, remoteJid);
            
            // Obter evolutionInstanceId da sessão
            Long evolutionInstanceId = messageSession.getEvolutionSession().getInstance().getId();
            
            // Enviar menu para o usuário
            evolutionApiService.sendMenuMessage(number, evolutionInstanceId);
            
            // Atualizar status do evento para MESSAGE-PROCESSED
            messageSessionService.updateCurrentEvent(messageSession.getId(), "MESSAGE-PROCESSED");
            
            log.info("Menu sent successfully to number: {}", number);
            
        } catch (Exception e) {
            log.error("Error processing MESSAGE-RECEIVED", e);
            throw new RuntimeException("Failed to process MESSAGE-RECEIVED", e);
        }
    }
    
    private void processUserResponse(MessageSession lastMessageSession, Map<String, Object> object) {
        try {
            log.info("Processing user response for MessageSession ID: {}", lastMessageSession.getId());
            
            // Converter objeto para WhatsAppMessageDTO
            WhatsAppMessageDTO messageData = objectMapper.convertValue(object, WhatsAppMessageDTO.class);
            
            // Extrair a mensagem do usuário
            String userMessage = extractUserMessage(messageData);
            log.info("User message: {}", userMessage);
            
            // Verificar se é opção 1 ou 2
            if ("1".equals(userMessage.trim())) {
                log.info("User selected option 1 - Detalhar história");
                processOptionOne(lastMessageSession, messageData, object);
            } else if ("2".equals(userMessage.trim())) {
                log.info("User selected option 2 - Encerrar sessão");
                processOptionTwo(lastMessageSession, messageData, object);
            } else {
                log.warn("Invalid option received: {}", userMessage);
                // Reenviar menu
                String remoteJid = messageData.getData().getKey().getRemoteJid();
                String number = extractPhoneNumber(remoteJid);
                Long evolutionInstanceId = lastMessageSession.getEvolutionSession().getInstance().getId();
                
                evolutionApiService.sendMessage(
                    number, 
                    "Opção inválida. Por favor, escolha 1 ou 2.", 
                    evolutionInstanceId
                );
            }
            
        } catch (Exception e) {
            log.error("Error processing user response", e);
            throw new RuntimeException("Failed to process user response", e);
        }
    }
    
    private void processOptionOne(MessageSession lastMessageSession, WhatsAppMessageDTO messageData, Map<String, Object> object) {
        log.info("Processing option 1 - Detalhar história");
        
        // Publicar evento MESSAGE-TASK-DETAIL
        Map<String, Object> taskDetailObject = new HashMap<>(object);
        taskDetailObject.put("messageSessionId", lastMessageSession.getId());
        
        MessageDTO taskDetailMessage = MessageDTO.builder()
                .event("MESSAGE-TASK-DETAIL")
                .object(taskDetailObject)
                .build();
        
        messageProducer.sendMessage(taskDetailMessage);
        
        log.info("MESSAGE-TASK-DETAIL event published");
        
        // Atualizar status
        messageSessionService.updateCurrentEvent(lastMessageSession.getId(), "AWAITING-TASK-DETAIL");
    }
    
    private void processOptionTwo(MessageSession lastMessageSession, WhatsAppMessageDTO messageData, Map<String, Object> object) {
        log.info("Processing option 2 - Encerrar sessão");
        
        // Publicar evento MESSAGE-SESSION-CLOSE
        Map<String, Object> closeObject = new HashMap<>(object);
        closeObject.put("messageSessionId", lastMessageSession.getId());
        
        MessageDTO closeMessage = MessageDTO.builder()
                .event("MESSAGE-SESSION-CLOSE")
                .object(closeObject)
                .build();
        
        messageProducer.sendMessage(closeMessage);
        
        log.info("MESSAGE-SESSION-CLOSE event published");
        
        // Atualizar status
        messageSessionService.updateCurrentEvent(lastMessageSession.getId(), "SESSION-CLOSING");
    }
    
    private String extractUserMessage(WhatsAppMessageDTO messageData) {
        Map<String, Object> message = messageData.getData().getMessage();
        if (message != null && message.containsKey("conversation")) {
            return message.get("conversation").toString();
        }
        return "";
    }
    
    private void processTaskDetailResponse(MessageSession lastMessageSession, Map<String, Object> object) {
        try {
            log.info("Processing task detail response for MessageSession ID: {}", lastMessageSession.getId());
            
            // Publicar evento TASK-DETAIL-CREATE
            Map<String, Object> taskCreateObject = new HashMap<>(object);
            taskCreateObject.put("messageSessionId", lastMessageSession.getId());
            
            MessageDTO taskCreateMessage = MessageDTO.builder()
                    .event("TASK-DETAIL-CREATE")
                    .object(taskCreateObject)
                    .build();
            
            messageProducer.sendMessage(taskCreateMessage);
            
            log.info("TASK-DETAIL-CREATE event published");
            
            // Atualizar status
            messageSessionService.updateCurrentEvent(lastMessageSession.getId(), "CREATING-TASK");
            
        } catch (Exception e) {
            log.error("Error processing task detail response", e);
            throw new RuntimeException("Failed to process task detail response", e);
        }
    }
    
    private String extractPhoneNumber(String remoteJid) {
        // Remove @s.whatsapp.net e outros sufixos
        if (remoteJid.contains("@")) {
            return remoteJid.substring(0, remoteJid.indexOf("@"));
        }
        return remoteJid;
    }
}
