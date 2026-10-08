package com.cese.process_entree_sortie.application.port.in.affectation;

import com.cese.process_entree_sortie.application.dto.affectation.sortie.AgentAffectationDTO;

import java.util.UUID;

// récupère la dernière affectation active d'un agent
public interface GetAffectationActiveByAgentApi {
    AgentAffectationDTO getAffectationAgent(UUID agentId);
}
