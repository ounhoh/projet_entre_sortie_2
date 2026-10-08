package com.cese.process_entree_sortie.application.port.in.agent;

import com.cese.process_entree_sortie.application.dto.agent.sortie.AgentDTO;
import com.cese.process_entree_sortie.domain.Agent.model.AgentPersonnel;

import java.util.UUID;

public interface GetAgentByIdApi {
    AgentDTO getAgentById(UUID agentId);
}
