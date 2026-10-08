package com.cese.process_entree_sortie.application.service.affectation;

import com.cese.process_entree_sortie.application.dto.affectation.entree.TerminerAffectationCommand;
import com.cese.process_entree_sortie.application.dto.affectation.sortie.AgentAffectationDTO;
import com.cese.process_entree_sortie.application.port.in.affectation.TerminerAffectationApi;
import com.cese.process_entree_sortie.application.port.out.agent.AffectationSpi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentAffectation;
import com.cese.process_entree_sortie.domain.Agent.model.AgentPersonnel;
import com.cese.process_entree_sortie.domain.utils.ValueObject.EtatAgent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TerminerAffectationService implements TerminerAffectationApi {
    
    private final AffectationSpi affectationSpi;
    private final AgentSpi agentSpi;
    
    public TerminerAffectationService(AffectationSpi affectationSpi, AgentSpi agentSpi) {
        this.affectationSpi = affectationSpi;
        this.agentSpi = agentSpi;
    }
    
    @Override
    public AgentAffectationDTO terminerAffectation(TerminerAffectationCommand command) {
        AgentAffectation affectation = affectationSpi.findById(command.affectationId())
            .orElseThrow(() -> new RuntimeException("Affectation non trouvée avec l'ID: " + command.affectationId()));
        
        // Modifier l'état de l'agent (pas de l'affectation)
        AgentPersonnel agent = agentSpi.findById(affectation.agentId())
            .orElseThrow(() -> new RuntimeException("Agent non trouvé avec l'ID: " + affectation.agentId()));
        
        AgentPersonnel agentModifie = agent.changerEtatAgent(EtatAgent.sortie);
        agentSpi.save(agentModifie);
        
        // L'affectation elle-même n'est pas modifiée
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