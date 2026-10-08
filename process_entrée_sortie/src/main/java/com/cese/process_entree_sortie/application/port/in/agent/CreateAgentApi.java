package com.cese.process_entree_sortie.application.port.in.agent;

import com.cese.process_entree_sortie.application.dto.agent.entree.CreateAgentCommand;
import com.cese.process_entree_sortie.application.dto.agent.sortie.AgentDTO;


public interface CreateAgentApi {
    AgentDTO createAgent(CreateAgentCommand command);
}
