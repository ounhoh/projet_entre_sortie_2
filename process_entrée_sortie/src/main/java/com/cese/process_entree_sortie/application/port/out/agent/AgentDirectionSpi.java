package com.cese.process_entree_sortie.application.port.out.agent;

import com.cese.process_entree_sortie.domain.Agent.model.AgentDirection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgentDirectionSpi {

    AgentDirection save(AgentDirection direction);
    Optional<AgentDirection> findById(UUID agentDirectionId);
    List<AgentDirection> findByAgentId(UUID agentId);
    List<AgentDirection> fingActivesByAgentId(UUID agentId);
    Optional<AgentDirection> findCurrentByAgentId(UUID agentId);
    List<AgentDirection> findByDirectionId(UUID directionId);

    void delete(AgentDirection agentDirection);
}
