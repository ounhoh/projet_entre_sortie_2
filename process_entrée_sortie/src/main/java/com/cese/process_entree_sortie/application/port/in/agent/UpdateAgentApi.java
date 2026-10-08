package com.cese.process_entree_sortie.application.port.in.agent;

import com.cese.process_entree_sortie.application.dto.agent.entree.UpdateAgentCommandCommand;
import com.cese.process_entree_sortie.application.dto.agent.sortie.AgentDTO;

public interface UpdateAgentApi {
    AgentDTO updateAgent(UpdateAgentCommandCommand command);
}
