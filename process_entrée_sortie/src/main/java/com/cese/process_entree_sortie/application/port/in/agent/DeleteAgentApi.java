package com.cese.process_entree_sortie.application.port.in.agent;

import java.util.UUID;

public interface DeleteAgentApi {
    void deleteAgent(UUID agentId);
}
