package com.cese.process_entree_sortie.infrastructure.persistence.repository;

import com.cese.process_entree_sortie.infrastructure.persistence.entity.AgentAffectationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AgentAffectationJpaRepository extends JpaRepository<AgentAffectationEntity, UUID> {
    List<AgentAffectationEntity> findByAgentId(UUID agentId);
}