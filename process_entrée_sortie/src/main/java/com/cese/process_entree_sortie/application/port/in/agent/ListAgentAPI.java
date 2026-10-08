package com.cese.process_entree_sortie.application.port.in.agent;

import com.cese.process_entree_sortie.application.dto.agent.entree.ListAgentsQueryCommand;
import com.cese.process_entree_sortie.application.dto.agent.sortie.AgentDTO;
import java.util.List;

public interface ListAgentAPI {
    List<AgentDTO> getsAgents(ListAgentsQueryCommand query);
}
