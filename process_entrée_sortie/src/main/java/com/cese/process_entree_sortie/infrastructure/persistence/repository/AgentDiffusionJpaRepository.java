package com.cese.process_entree_sortie.infrastructure.persistence.repository;

import com.cese.process_entree_sortie.infrastructure.persistence.entity.AgentDiffusionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentDiffusionJpaRepository extends JpaRepository<AgentDiffusionEntity, UUID> {
    Optional<AgentDiffusionEntity> findByAgentId(UUID agentId);
}
