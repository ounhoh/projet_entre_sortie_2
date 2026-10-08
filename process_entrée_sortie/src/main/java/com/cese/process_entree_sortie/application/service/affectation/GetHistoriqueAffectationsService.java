package com.cese.process_entree_sortie.application.service.affectation;

import com.cese.process_entree_sortie.application.dto.affectation.sortie.AgentAffectationDTO;
import com.cese.process_entree_sortie.application.port.in.affectation.GetHistoriqueAffectationsApi;
import com.cese.process_entree_sortie.application.port.out.agent.AffectationSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentAffectation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetHistoriqueAffectationsService implements GetHistoriqueAffectationsApi {
    
    private final AffectationSpi affectationSpi;
    
    public GetHistoriqueAffectationsService(AffectationSpi affectationSpi) {
        this.affectationSpi = affectationSpi;
    }
    
    @Override
    public List<AgentAffectationDTO> getHistoriqueAffectation(UUID agentId) {
        List<AgentAffectation> affectations = affectationSpi.findByAgentId(agentId);
        
        return affectations.stream()
            .map(this::convertirEnDTO)
            .collect(Collectors.toList());
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