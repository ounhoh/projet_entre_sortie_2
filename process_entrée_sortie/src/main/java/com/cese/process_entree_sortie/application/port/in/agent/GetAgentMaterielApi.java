package com.cese.process_entree_sortie.application.port.in.agent;

import com.cese.process_entree_sortie.domain.Agent.model.AgentMaterielEtDroit;

import java.util.Optional;
import java.util.UUID;

public interface GetAgentMaterielApi {
    Optional<AgentMaterielEtDroit> getAgentMateriel(UUID agentId);
}
