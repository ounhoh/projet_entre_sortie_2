package com.cese.process_entree_sortie.application.service.agent;

import com.cese.process_entree_sortie.application.dto.agent.sortie.AgentDTO;
import com.cese.process_entree_sortie.application.port.in.agent.GetAgentByCodeApi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentSpi;
import com.cese.process_entree_sortie.application.port.out.direction.DirectionSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentPersonnel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetAgentByCodeService implements GetAgentByCodeApi {
    
    private final AgentSpi agentSpi;
    private final DirectionSpi directionSpi;
    
    public GetAgentByCodeService(AgentSpi agentSpi, DirectionSpi directionSpi) {
        this.agentSpi = agentSpi;
        this.directionSpi = directionSpi;
    }
    
    @Override
    public AgentDTO getAgentByCode(String codeAgent) {
        AgentPersonnel agent = agentSpi.findByCodeAgent(codeAgent)
            .orElseThrow(() -> new RuntimeException("Agent non trouvé avec le code: " + codeAgent));
        
        return AgentConverter.convertirEnDTO(agent, directionSpi);
    }
}