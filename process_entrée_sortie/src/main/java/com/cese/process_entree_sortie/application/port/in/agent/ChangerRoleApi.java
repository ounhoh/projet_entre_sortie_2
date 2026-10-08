package com.cese.process_entree_sortie.application.port.in.agent;

import com.cese.process_entree_sortie.application.dto.agent.entree.ChangerRoleCommand;
import com.cese.process_entree_sortie.application.dto.agent.sortie.AgentDTO;

public interface ChangerRoleApi {
    AgentDTO changerRole(ChangerRoleCommand roleCommand);
}
