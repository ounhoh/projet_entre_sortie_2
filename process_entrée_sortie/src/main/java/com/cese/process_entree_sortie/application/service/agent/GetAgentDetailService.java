package com.cese.process_entree_sortie.application.service.agent;

import com.cese.process_entree_sortie.application.dto.agent.sortie.AgentDTO;
import com.cese.process_entree_sortie.application.port.in.agent.GetAgentDetail;
import com.cese.process_entree_sortie.application.port.out.agent.AgentSpi;
import com.cese.process_entree_sortie.application.port.out.direction.DirectionSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentPersonnel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetAgentDetailService implements GetAgentDetail {
    
    private final AgentSpi agentSpi;
    private final DirectionSpi directionSpi;
    
    public GetAgentDetailService(AgentSpi agentSpi, DirectionSpi directionSpi) {
        this.agentSpi = agentSpi;
        this.directionSpi = directionSpi;
    }
    
    @Override
    public AgentDTO getAgentDetail(UUID agentId) {
        AgentPersonnel agent = agentSpi.findById(agentId)
            .orElseThrow(() -> new RuntimeException("Agent non trouvé avec l'ID: " + agentId));
        
        return AgentConverter.convertirEnDTO(agent, directionSpi);
    }
}