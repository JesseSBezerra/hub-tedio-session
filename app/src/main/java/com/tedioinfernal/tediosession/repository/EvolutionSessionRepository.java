package com.tedioinfernal.tediosession.repository;

import com.tedioinfernal.tediosession.entity.EvolutionInstance;
import com.tedioinfernal.tediosession.entity.EvolutionSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EvolutionSessionRepository extends JpaRepository<EvolutionSession, Long> {
    
    Optional<EvolutionSession> findByInstanceAndRemoteJid(EvolutionInstance instance, String remoteJid);
    
    Optional<EvolutionSession> findByInstanceAndRemoteJidAndStatus(
            EvolutionInstance instance, 
            String remoteJid, 
            String status
    );
}
