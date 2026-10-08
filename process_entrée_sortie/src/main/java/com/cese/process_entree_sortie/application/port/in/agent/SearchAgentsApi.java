package com.cese.process_entree_sortie.application.port.in.agent;

import com.cese.process_entree_sortie.application.dto.agent.entree.SearchAgentsCommand;
import com.cese.process_entree_sortie.application.dto.agent.sortie.AgentDTO;

public interface SearchAgentsApi {
    AgentDTO searchAgents(SearchAgentsCommand searchAgentsCommand);
}
