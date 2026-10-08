package com.cese.process_entree_sortie.infrastructure.persistence.repository;

import com.cese.process_entree_sortie.infrastructure.persistence.entity.AgentMaterielEtDroitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentMaterielEtDroitJpaRepository extends JpaRepository<AgentMaterielEtDroitEntity, UUID> {
    Optional<AgentMaterielEtDroitEntity> findByAgentId(UUID agentId);
}
