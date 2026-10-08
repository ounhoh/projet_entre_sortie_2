package com.cese.process_entree_sortie.application.port.in.agent;

import java.util.List;
import java.util.UUID;

public interface GetAgentDiffusionApi {
    List<String> getAgentDiffusion(UUID agentId);
}
