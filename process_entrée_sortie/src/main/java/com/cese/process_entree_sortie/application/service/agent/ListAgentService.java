package com.cese.process_entree_sortie.application.service.agent;

import com.cese.process_entree_sortie.application.dto.agent.entree.ListAgentsQueryCommand;
import com.cese.process_entree_sortie.application.dto.agent.sortie.AgentDTO;
import com.cese.process_entree_sortie.application.port.in.agent.ListAgentAPI;
import com.cese.process_entree_sortie.application.port.out.agent.AgentSpi;
import com.cese.process_entree_sortie.application.port.out.direction.DirectionSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentPersonnel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;
@Service
@Transactional(readOnly = true)
public class ListAgentService implements ListAgentAPI {
    
    private final AgentSpi agentSpi;
    private final DirectionSpi directionSpi;
    
    public ListAgentService(AgentSpi agentSpi, DirectionSpi directionSpi) {
        this.agentSpi = agentSpi;
        this.directionSpi = directionSpi;
    }
    
    @Override
    public List<AgentDTO> getsAgents(ListAgentsQueryCommand query) {
        List<AgentPersonnel> agents = agentSpi.findAll(
            query.page(), 
            query.size(), 
            query.sortBy(), 
            query.direction()
        );
        
        return agents.stream()
            .map(agent -> AgentConverter.convertirEnDTO(agent, directionSpi))
            .collect(Collectors.toList());
    }
}