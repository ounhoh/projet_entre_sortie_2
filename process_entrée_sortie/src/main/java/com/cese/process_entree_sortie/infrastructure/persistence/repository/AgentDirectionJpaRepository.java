package com.cese.process_entree_sortie.infrastructure.persistence.repository;

import com.cese.process_entree_sortie.infrastructure.persistence.entity.AgentDirectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AgentDirectionJpaRepository extends JpaRepository<AgentDirectionEntity, UUID> {
    List<AgentDirectionEntity> findByAgentPersonnelId(UUID agentPersonnelId);
    
    @Query("SELECT ad FROM AgentDirectionEntity ad WHERE ad.agentPersonnelId = :agentId AND (ad.dateDepart IS NULL OR ad.dateDepart > :today)")
    List<AgentDirectionEntity> findActivesByAgentId(@Param("agentId") UUID agentId, @Param("today") LocalDate today);
    
    @Query("SELECT ad FROM AgentDirectionEntity ad WHERE ad.agentPersonnelId = :agentId AND (ad.dateDepart IS NULL OR ad.dateDepart > :today) ORDER BY ad.dateArrivee DESC")
    List<AgentDirectionEntity> findCurrentByAgentId(@Param("agentId") UUID agentId, @Param("today") LocalDate today);

    List<AgentDirectionEntity> findByDirectionId(UUID directionId);
}