package com.cese.process_entree_sortie.application.service.affectation;

import com.cese.process_entree_sortie.application.dto.affectation.sortie.AgentAffectationDTO;
import com.cese.process_entree_sortie.application.port.in.affectation.GetAffectationByIdApi;
import com.cese.process_entree_sortie.application.port.out.agent.AffectationSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentAffectation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetAffectationByIdService implements GetAffectationByIdApi {
    
    private final AffectationSpi affectationSpi;
    
    public GetAffectationByIdService(AffectationSpi affectationSpi) {
        this.affectationSpi = affectationSpi;
    }
    
    @Override
    public AgentAffectationDTO getAffectationById(UUID affectationId) {
        AgentAffectation affectation = affectationSpi.findById(affectationId)
            .orElseThrow(() -> new RuntimeException("Affectation non trouvée avec l'ID: " + affectationId));
        
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