package com.cese.process_entree_sortie.application.service.affectation;

import com.cese.process_entree_sortie.application.dto.affectation.sortie.AgentAffectationDTO;
import com.cese.process_entree_sortie.application.port.in.affectation.ActiverAffectationApi;
import com.cese.process_entree_sortie.application.port.out.agent.AffectationSpi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentAffectation;
import com.cese.process_entree_sortie.domain.Agent.model.AgentPersonnel;
import com.cese.process_entree_sortie.domain.utils.ValueObject.EtatAgent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ActiverAffectationService implements ActiverAffectationApi {
    
    private final AffectationSpi affectationSpi;
    private final AgentSpi agentSpi;
    
    public ActiverAffectationService(AffectationSpi affectationSpi, AgentSpi agentSpi) {
        this.affectationSpi = affectationSpi;
        this.agentSpi = agentSpi;
    }
    
    @Override
    public AgentAffectationDTO activerAffectation(UUID affectationId) {
        AgentAffectation affectation = affectationSpi.findById(affectationId)
            .orElseThrow(() -> new RuntimeException("Affectation non trouvée avec l'ID: " + affectationId));
        
        // Modifier l'état de l'agent (pas de l'affectation) - passer de "entree" à "actif"
        AgentPersonnel agent = agentSpi.findById(affectation.agentId())
            .orElseThrow(() -> new RuntimeException("Agent non trouvé avec l'ID: " + affectation.agentId()));
        
        AgentPersonnel agentModifie = agent.changerEtatAgent(EtatAgent.actif);
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