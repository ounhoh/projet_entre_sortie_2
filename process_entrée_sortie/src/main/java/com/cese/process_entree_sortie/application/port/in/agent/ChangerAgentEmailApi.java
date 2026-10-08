package com.cese.process_entree_sortie.application.port.in.agent;

import com.cese.process_entree_sortie.application.dto.agent.entree.ChangerEmailCommand;
import com.cese.process_entree_sortie.application.dto.agent.sortie.AgentDTO;

public interface ChangerAgentEmailApi {
    AgentDTO changerAgentEmail(ChangerEmailCommand changerEmailCommand);
}
