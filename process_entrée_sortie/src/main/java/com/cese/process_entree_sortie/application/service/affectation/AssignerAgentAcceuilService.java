package com.cese.process_entree_sortie.application.service.affectation;

import com.cese.process_entree_sortie.application.dto.affectation.entree.AssignerAgentAcceuilCommand;
import com.cese.process_entree_sortie.application.dto.affectation.sortie.AgentAffectationDTO;
import com.cese.process_entree_sortie.application.port.in.affectation.AssignerAgentAcceuil;
import com.cese.process_entree_sortie.application.port.out.agent.AffectationSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentAffectation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AssignerAgentAcceuilService implements AssignerAgentAcceuil {
    
    private final AffectationSpi affectationSpi;
    
    public AssignerAgentAcceuilService(AffectationSpi affectationSpi) {
        this.affectationSpi = affectationSpi;
    }
    
    @Override
    public AgentAffectationDTO assignerAgentAcceuil(AssignerAgentAcceuilCommand command) {
        AgentAffectation affectation = affectationSpi.findById(command.affectationId())
            .orElseThrow(() -> new RuntimeException("Affectation non trouvée avec l'ID: " + command.affectationId()));
        
        // Assigner l'agent d'accueil
        AgentAffectation affectationModifiee = affectation.changerAgentAcceuil(command.agentAcceuilId());
        
        AgentAffectation affectationSauvegardee = affectationSpi.save(affectationModifiee);
        
        return convertirEnDTO(affectationSauvegardee);
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