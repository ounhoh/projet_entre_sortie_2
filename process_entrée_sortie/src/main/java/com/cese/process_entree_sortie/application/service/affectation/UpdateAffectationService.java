package com.cese.process_entree_sortie.application.service.affectation;

import com.cese.process_entree_sortie.application.dto.affectation.entree.UpdateAffectationCommand;
import com.cese.process_entree_sortie.application.dto.affectation.sortie.AgentAffectationDTO;
import com.cese.process_entree_sortie.application.port.in.affectation.UpdateAffectationApi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentSpi;
import com.cese.process_entree_sortie.application.port.out.agent.AffectationSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentAffectation;
import com.cese.process_entree_sortie.domain.Agent.model.AgentPersonnel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class UpdateAffectationService implements UpdateAffectationApi {
    
    private final AgentSpi agentSpi;
    private final AffectationSpi affectationSpi;
    
    public UpdateAffectationService(AgentSpi agentSpi, AffectationSpi affectationSpi) {
        this.agentSpi = agentSpi;
        this.affectationSpi = affectationSpi;
    }
    
    @Override
    public AgentAffectationDTO updateAffectation(UpdateAffectationCommand command) {
        // Récupérer l'agent
        AgentPersonnel agent = agentSpi.findById(command.agentId())
            .orElseThrow(() -> new RuntimeException("Agent non trouvé avec l'ID: " + command.agentId()));
        
        // Créer une nouvelle affectation avec les données de la commande
        AgentAffectation nouvelleAffectation = new AgentAffectation(
            command.fonction(),
            null, // agentResponsableId - à assigner séparément si nécessaire
            null, // agentAcceuilId - à assigner séparément si nécessaire
            command.directionId(),
            command.agentId()
        );
        
        // Sauvegarder la nouvelle affectation d'abord
        AgentAffectation affectationSauvegardee = affectationSpi.save(nouvelleAffectation);
        
        // Créer un nouvel agent avec la nouvelle affectation ajoutée à la liste
        java.util.List<AgentAffectation> nouvellesAffectations = new java.util.ArrayList<>(agent.agentAffectations());
        nouvellesAffectations.addFirst(affectationSauvegardee);
        
        AgentPersonnel agentModifie = new AgentPersonnel(
            agent.id(),
            agent.nom(),
            agent.prenom(),
            agent.email(),
            agent.role(),
            agent.etatAgent(), // Conserver l'état existant
            agent.agentDirections(),
            java.util.List.copyOf(nouvellesAffectations),
            agent.agentMaterielEtDroit()
        );
        
        // Sauvegarder l'agent avec la nouvelle affectation
        agentSpi.save(agentModifie);
        
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