package com.cese.process_entree_sortie.application.port.out.agent;

import com.cese.process_entree_sortie.domain.Agent.model.AgentAffectation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AffectationSpi {
    // sauvegarder affectation
    AgentAffectation save(AgentAffectation affectation);
    // trouver avec l'id
    Optional<AgentAffectation> findById(UUID affectationId);
    // trouver avec l'id de l'agent
    List<AgentAffectation> findByAgentId(UUID agentId);
    List<AgentAffectation> fingActivesByAgentId(UUID agentId);
    // trouver l'affectation actuel
    Optional<AgentAffectation> findCurrentByAgentId(UUID agentId);

    // suprimer une affectation
    void delete(AgentAffectation affectation);
}