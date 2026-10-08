package com.cese.process_entree_sortie.application.port.in.agent;

import com.cese.process_entree_sortie.application.dto.agent.sortie.AgentDTO;

import java.util.UUID;

public interface GetAgentDetail {
    AgentDTO getAgentDetail(UUID agentId);
}
