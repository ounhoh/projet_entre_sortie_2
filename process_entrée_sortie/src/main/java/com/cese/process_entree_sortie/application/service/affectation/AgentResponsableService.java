package com.cese.process_entree_sortie.application.service.affectation;

import com.cese.process_entree_sortie.application.dto.affectation.entree.AssignerResponsableCommand;
import com.cese.process_entree_sortie.application.dto.affectation.sortie.AgentAffectationDTO;
import com.cese.process_entree_sortie.application.port.in.affectation.AgentResponsableApi;
import com.cese.process_entree_sortie.application.port.out.agent.AffectationSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentAffectation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AgentResponsableService implements AgentResponsableApi {
    
    private final AffectationSpi affectationSpi;
    
    public AgentResponsableService(AffectationSpi affectationSpi) {
        this.affectationSpi = affectationSpi;
    }
    
    @Override
    public AgentAffectationDTO assignerResponsable(AssignerResponsableCommand command) {
        AgentAffectation affectation = affectationSpi.findById(command.affectationId())
            .orElseThrow(() -> new RuntimeException("Affectation non trouvée avec l'ID: " + command.affectationId()));
        
        // Assigner le responsable
        AgentAffectation affectationModifiee = affectation.changerAgentResponsable(command.agentResponsable());
        
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