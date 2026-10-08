package com.cese.process_entree_sortie.application.service.agent;

import com.cese.process_entree_sortie.application.dto.agent.entree.SearchAgentsCommand;
import com.cese.process_entree_sortie.application.dto.agent.sortie.AgentDTO;
import com.cese.process_entree_sortie.application.port.in.agent.SearchAgentsApi;
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
public class SearchAgentsService implements SearchAgentsApi {
    
    private final AgentSpi agentSpi;
    private final DirectionSpi directionSpi;
    
    public SearchAgentsService(AgentSpi agentSpi, DirectionSpi directionSpi) {
        this.agentSpi = agentSpi;
        this.directionSpi = directionSpi;
    }
    
    @Override
    public AgentDTO searchAgents(SearchAgentsCommand searchAgentsCommand) {
        List<AgentPersonnel> agents = agentSpi.search(
            searchAgentsCommand.termeDeRecherche(),
            searchAgentsCommand.role(),
            searchAgentsCommand.directionId()
        );
        
        List<AgentDTO> agentsDTO = agents.stream()
            .map(agent -> AgentConverter.convertirEnDTO(agent, directionSpi))
            .collect(Collectors.toList());
        
        // Retourner le premier agent ou une liste vide
        // Note: Le DTO devrait probablement être une liste, mais suivant l'interface
        return agentsDTO.isEmpty() ? null : agentsDTO.get(0);
    }
}