package com.tedioinfernal.tediosession.service;

import com.tedioinfernal.tediosession.entity.EvolutionInstance;
import com.tedioinfernal.tediosession.entity.EvolutionSession;
import com.tedioinfernal.tediosession.repository.EvolutionInstanceRepository;
import com.tedioinfernal.tediosession.repository.EvolutionSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final EvolutionInstanceRepository instanceRepository;
    private final EvolutionSessionRepository sessionRepository;

    @Transactional(readOnly = true)
    public Optional<EvolutionInstance> findInstanceByName(String instanceName) {
        log.debug("Finding instance by name: {}", instanceName);
        return instanceRepository.findByInstanceNameWithEvolution(instanceName);
    }

    @Transactional
    public EvolutionSession findOrCreateSession(EvolutionInstance instance, String remoteJid) {
        log.info("Finding or creating session for instance: {} and remoteJid: {}", 
                 instance.getInstanceName(), remoteJid);
        
        Optional<EvolutionSession> existingSession = sessionRepository
                .findByInstanceAndRemoteJidAndStatus(instance, remoteJid, "ACTIVE");
        
        if (existingSession.isPresent()) {
            log.info("Active session found for remoteJid: {}", remoteJid);
            return existingSession.get();
        }
        
        // Verificar se existe uma sessão inativa
        Optional<EvolutionSession> inactiveSession = sessionRepository
                .findByInstanceAndRemoteJid(instance, remoteJid);
        
        if (inactiveSession.isPresent()) {
            log.info("Reactivating inactive session for remoteJid: {}", remoteJid);
            EvolutionSession session = inactiveSession.get();
            session.setStatus("ACTIVE");
            return sessionRepository.save(session);
        }
        
        // Criar nova sessão
        log.info("Creating new session for remoteJid: {}", remoteJid);
        EvolutionSession newSession = EvolutionSession.builder()
                .instance(instance)
                .remoteJid(remoteJid)
                .status("ACTIVE")
                .build();
        
        return sessionRepository.save(newSession);
    }

    @Transactional
    public void deactivateSession(Long sessionId) {
        log.info("Deactivating session with id: {}", sessionId);
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.setStatus("INACTIVE");
            sessionRepository.save(session);
            log.info("Session deactivated successfully");
        });
    }

    @Transactional
    public void deactivateSessionByInstanceAndRemoteJid(EvolutionInstance instance, String remoteJid) {
        log.info("Deactivating session for instance: {} and remoteJid: {}", 
                 instance.getInstanceName(), remoteJid);
        
        sessionRepository.findByInstanceAndRemoteJid(instance, remoteJid).ifPresent(session -> {
            session.setStatus("INACTIVE");
            sessionRepository.save(session);
            log.info("Session deactivated successfully");
        });
    }
}
