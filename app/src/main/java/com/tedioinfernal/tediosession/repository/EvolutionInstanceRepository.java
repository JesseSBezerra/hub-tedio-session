package com.tedioinfernal.tediosession.repository;

import com.tedioinfernal.tediosession.entity.EvolutionInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EvolutionInstanceRepository extends JpaRepository<EvolutionInstance, Long> {
    
    Optional<EvolutionInstance> findByInstanceName(String instanceName);
    
    @Query("SELECT ei FROM EvolutionInstance ei " +
           "LEFT JOIN FETCH ei.evolution e " +
           "WHERE ei.instanceName = :instanceName")
    Optional<EvolutionInstance> findByInstanceNameWithEvolution(@Param("instanceName") String instanceName);
}
