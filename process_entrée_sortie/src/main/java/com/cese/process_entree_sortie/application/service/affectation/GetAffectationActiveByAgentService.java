package com.cese.process_entree_sortie.application.service.affectation;

import com.cese.process_entree_sortie.application.dto.affectation.sortie.AgentAffectationDTO;
import com.cese.process_entree_sortie.application.port.in.affectation.GetAffectationActiveByAgentApi;
import com.cese.process_entree_sortie.application.port.out.agent.AffectationSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentAffectation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetAffectationActiveByAgentService implements GetAffectationActiveByAgentApi {
    
    private final AffectationSpi affectationSpi;
    
    public GetAffectationActiveByAgentService(AffectationSpi affectationSpi) {
        this.affectationSpi = affectationSpi;
    }
    
    @Override
    public AgentAffectationDTO getAffectationAgent(UUID agentId) {
        AgentAffectation affectation = affectationSpi.findCurrentByAgentId(agentId)
            .orElseThrow(() -> new RuntimeException("Aucune affectation active trouvée pour l'agent: " + agentId));
        
        return convertirEnDTO(affectation);
    }
    
    private AgentAffectationDTO convertirEnDTO(AgentAffectation affectation) {
        return new AgentAffectationDTO(
            affectation.Fonction(),
            affectation.agentResponsableId(),
            affectation.agentAcceuilId(),
            affectation.directionId(),
            affectation.agentId()
        );
    }
}