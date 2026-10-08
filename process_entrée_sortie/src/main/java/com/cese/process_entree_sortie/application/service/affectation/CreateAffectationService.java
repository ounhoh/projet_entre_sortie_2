package com.cese.process_entree_sortie.application.service.affectation;

import com.cese.process_entree_sortie.application.dto.affectation.entree.CreateAffectationCommand;
import com.cese.process_entree_sortie.application.dto.affectation.sortie.AgentAffectationDTO;
import com.cese.process_entree_sortie.application.port.in.affectation.CreateAffectationApi;
import com.cese.process_entree_sortie.application.port.out.agent.AffectationSpi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentAffectation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class CreateAffectationService implements CreateAffectationApi {
    
    private final AffectationSpi affectationSpi;
    private final AgentSpi agentSpi;
    
    public CreateAffectationService(AffectationSpi affectationSpi, AgentSpi agentSpi) {
        this.affectationSpi = affectationSpi;
        this.agentSpi = agentSpi;
    }
    
    @Override
    public AgentAffectationDTO createAffectation(CreateAffectationCommand command) {
        // Vérifier que l'agent existe
        agentSpi.findById(command.agentId())
            .orElseThrow(() -> new RuntimeException("Agent non trouvé avec l'ID: " + command.agentId()));
        
        AgentAffectation affectation = new AgentAffectation(
            command.fonction(),
            null, // agentResponsableId
            null, // agentAcceuilId
            command.direction(),
            command.agentId()
        );
        
        AgentAffectation affectationSauvegardee = affectationSpi.save(affectation);
        
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