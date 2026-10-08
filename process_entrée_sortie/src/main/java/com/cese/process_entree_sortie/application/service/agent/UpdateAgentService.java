package com.cese.process_entree_sortie.application.service.agent;

import com.cese.process_entree_sortie.application.dto.agent.entree.UpdateAgentCommandCommand;
import com.cese.process_entree_sortie.application.dto.agent.sortie.AgentDTO;
import com.cese.process_entree_sortie.application.port.in.agent.UpdateAgentApi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentPersonnel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateAgentService implements UpdateAgentApi {
    
    private final AgentSpi agentSpi;
    
    public UpdateAgentService(AgentSpi agentSpi) {
        this.agentSpi = agentSpi;
    }
    
    @Override
    public AgentDTO updateAgent(UpdateAgentCommandCommand command) {
        AgentPersonnel agentExistant = agentSpi.findById(command.agentId())
            .orElseThrow(() -> new RuntimeException("Agent non trouvé avec l'ID: " + command.agentId()));

        agentSpi.findByEmail(command.email())
            .filter(agent -> !agent.id().equals(command.agentId()))
            .ifPresent(agent -> {
                if (agent.etatAgent() == com.cese.process_entree_sortie.domain.utils.ValueObject.EtatAgent.ancien) {
                    agentSpi.save(agent.changerEtatAgent(com.cese.process_entree_sortie.domain.utils.ValueObject.EtatAgent.ancien));
                } else {
                    throw new RuntimeException("Adresse email déjà utilisée par un agent actif.");
                }
            });
        
        AgentPersonnel agentModifie = new AgentPersonnel(
            command.agentId(),
            command.nom(),
            command.prenom(),
            command.email(),
            agentExistant.role(),
            agentExistant.etatAgent(), // Conserver l'état existant
            agentExistant.agentDirections(),
            agentExistant.agentAffectations(),
            agentExistant.agentMaterielEtDroit()
        );
        
        AgentPersonnel agentSauvegarde = agentSpi.save(agentModifie);
        
        return AgentConverter.convertirEnDTO(agentSauvegarde);
    }
}