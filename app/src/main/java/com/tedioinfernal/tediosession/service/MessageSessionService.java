package com.tedioinfernal.tediosession.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tedioinfernal.tediosession.entity.EvolutionSession;
import com.tedioinfernal.tediosession.entity.MessageSession;
import com.tedioinfernal.tediosession.repository.MessageSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageSessionService {

    private final MessageSessionRepository messageSessionRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public MessageSession saveMessageSession(
            EvolutionSession evolutionSession,
            Map<String, Object> messageData,
            String currentEvent,
            Long messageTimestamp,
            String remoteJid,
            Boolean fromMe,
            String messageType,
            String pushName) {
        
        try {
            log.info("Saving message session for remoteJid: {}", remoteJid);
            
            String messageContent = objectMapper.writeValueAsString(messageData);
            
            MessageSession messageSession = MessageSession.builder()
                    .evolutionSession(evolutionSession)
                    .messageContent(messageContent)
                    .currentEvent(currentEvent)
                    .messageTimestamp(messageTimestamp)
                    .remoteJid(remoteJid)
                    .fromMe(fromMe)
                    .messageType(messageType)
                    .pushName(pushName)
                    .build();
            
            MessageSession saved = messageSessionRepository.save(messageSession);
            log.info("Message session saved with ID: {}", saved.getId());
            
            return saved;
            
        } catch (Exception e) {
            log.error("Error saving message session", e);
            throw new RuntimeException("Failed to save message session", e);
        }
    }

    @Transactional
    public void updateCurrentEvent(Long messageSessionId, String newEvent) {
        log.info("Updating message session {} to event: {}", messageSessionId, newEvent);
        
        messageSessionRepository.findById(messageSessionId).ifPresent(messageSession -> {
            messageSession.setCurrentEvent(newEvent);
            messageSessionRepository.save(messageSession);
            log.info("Message session updated successfully");
        });
    }

    @Transactional(readOnly = true)
    public Optional<MessageSession> findById(Long id) {
        Optional<MessageSession> messageSession = messageSessionRepository.findById(id);
        // Forçar carregamento da sessão e instância para evitar LazyInitializationException
        messageSession.ifPresent(ms -> {
            ms.getEvolutionSession().getInstance().getId();
        });
        return messageSession;
    }

    @Transactional(readOnly = true)
    public Optional<MessageSession> findLatestByRemoteJid(String remoteJid) {
        return messageSessionRepository.findTopByRemoteJidOrderByCreatedAtDesc(remoteJid);
    }
}
