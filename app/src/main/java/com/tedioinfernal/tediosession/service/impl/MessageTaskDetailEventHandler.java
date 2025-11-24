package com.tedioinfernal.tediosession.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tedioinfernal.tediosession.annotation.EventHandler;
import com.tedioinfernal.tediosession.dto.WhatsAppMessageDTO;
import com.tedioinfernal.tediosession.entity.MessageSession;
import com.tedioinfernal.tediosession.service.EventHandlerService;
import com.tedioinfernal.tediosession.service.EvolutionApiService;
import com.tedioinfernal.tediosession.service.MessageSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;
import java.util.Optional;

@Slf4j
@EventHandler("MESSAGE-TASK-DETAIL")
@RequiredArgsConstructor
public class MessageTaskDetailEventHandler implements EventHandlerService {

    private final MessageSessionService messageSessionService;
    private final EvolutionApiService evolutionApiService;
    private final ObjectMapper objectMapper;

    @Override
    @Async
    public void handle(Map<String, Object> object) {
        log.info("Processing MESSAGE-TASK-DETAIL event");
        
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
            log.info("Processing task detail for MessageSession ID: {}", messageSession.getId());
            
            // Converter objeto para WhatsAppMessageDTO
            WhatsAppMessageDTO messageData = objectMapper.convertValue(object, WhatsAppMessageDTO.class);
            
            // Extrair informações
            String remoteJid = messageData.getData().getKey().getRemoteJid();
            String number = extractPhoneNumber(remoteJid);
            Long evolutionInstanceId = messageSession.getEvolutionSession().getInstance().getId();
            
            // Enviar mensagem solicitando detalhamento
            String requestMessage = "📝 *Detalhe sua história*\n\n" +
                    "Por favor, conte-nos mais detalhes sobre sua solicitação.\n\n" +
                    "Descreva o que você precisa e iremos te ajudar! ✍️";
            
            evolutionApiService.sendMessage(number, requestMessage, evolutionInstanceId);
            
            log.info("Task detail request sent to number: {}", number);
            
            // Atualizar status
            messageSessionService.updateCurrentEvent(messageSession.getId(), "TASK-DETAIL-REQUESTED");
            
            log.info("MESSAGE-TASK-DETAIL event processed successfully");
            
        } catch (Exception e) {
            log.error("Error processing MESSAGE-TASK-DETAIL event", e);
            throw new RuntimeException("Failed to process MESSAGE-TASK-DETAIL event", e);
        }
    }
    
    private Long extractMessageSessionId(Map<String, Object> object) {
        Object idObj = object.get("messageSessionId");
        if (idObj instanceof Number) {
            return ((Number) idObj).longValue();
        }
        return null;
    }
    
    private String extractPhoneNumber(String remoteJid) {
        // Remove @s.whatsapp.net e outros sufixos
        if (remoteJid.contains("@")) {
            return remoteJid.substring(0, remoteJid.indexOf("@"));
        }
        return remoteJid;
    }
}
