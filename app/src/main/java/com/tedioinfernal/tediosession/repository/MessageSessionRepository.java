package com.tedioinfernal.tediosession.repository;

import com.tedioinfernal.tediosession.entity.MessageSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageSessionRepository extends JpaRepository<MessageSession, Long> {
    
    List<MessageSession> findByCurrentEvent(String currentEvent);
    
    Optional<MessageSession> findTopByRemoteJidOrderByCreatedAtDesc(String remoteJid);
    
    List<MessageSession> findByEvolutionSessionIdOrderByCreatedAtDesc(Long evolutionSessionId);
}
