package com.cese.process_entree_sortie.application.port.in.agent;

import java.util.UUID;

public interface DeleteAgentUseCase {
    void deleteAgent(UUID agentId);
}
