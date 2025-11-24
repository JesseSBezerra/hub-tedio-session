package com.tedioinfernal.tediosession.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tedioinfernal.tediosession.annotation.EventHandler;
import com.tedioinfernal.tediosession.dto.WhatsAppMessageDTO;
import com.tedioinfernal.tediosession.entity.EvolutionSession;
import com.tedioinfernal.tediosession.entity.MessageSession;
import com.tedioinfernal.tediosession.service.EventHandlerService;
import com.tedioinfernal.tediosession.service.EvolutionApiService;
import com.tedioinfernal.tediosession.service.MessageSessionService;
import com.tedioinfernal.tediosession.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;
import java.util.Optional;

@Slf4j
@EventHandler("MESSAGE-SESSION-CLOSE")
@RequiredArgsConstructor
public class MessageSessionCloseEventHandler implements EventHandlerService {

    private final MessageSessionService messageSessionService;
    private final SessionService sessionService;
    private final EvolutionApiService evolutionApiService;
    private final ObjectMapper objectMapper;

    @Override
    @Async
    public void handle(Map<String, Object> object) {
        log.info("Processing MESSAGE-SESSION-CLOSE event");
        
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
            log.info("Closing session for MessageSession ID: {}", messageSession.getId());
            
            // Converter objeto para WhatsAppMessageDTO
            WhatsAppMessageDTO messageData = objectMapper.convertValue(object, WhatsAppMessageDTO.class);
            
            // Extrair informações
            String remoteJid = messageData.getData().getKey().getRemoteJid();
            String number = extractPhoneNumber(remoteJid);
            Long evolutionInstanceId = messageSession.getEvolutionSession().getInstance().getId();
            
            // Enviar mensagem de agradecimento
            String thankYouMessage = "🙏 *Obrigado pelo contato!*\n\n" +
                    "Sua sessão foi encerrada com sucesso.\n\n" +
                    "Caso precise de algo, estamos sempre à disposição!\n\n" +
                    "Até breve! 👋";
            
            evolutionApiService.sendMessage(number, thankYouMessage, evolutionInstanceId);
            
            log.info("Thank you message sent to number: {}", number);
            
            // Inativar a sessão
            EvolutionSession evolutionSession = messageSession.getEvolutionSession();
            sessionService.deactivateSession(evolutionSession.getId());
            
            log.info("Evolution session {} deactivated", evolutionSession.getId());
            
            // Atualizar status da mensagem
            messageSessionService.updateCurrentEvent(messageSession.getId(), "SESSION-CLOSED");
            
            log.info("MESSAGE-SESSION-CLOSE event processed successfully");
            
        } catch (Exception e) {
            log.error("Error processing MESSAGE-SESSION-CLOSE event", e);
            throw new RuntimeException("Failed to process MESSAGE-SESSION-CLOSE event", e);
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
