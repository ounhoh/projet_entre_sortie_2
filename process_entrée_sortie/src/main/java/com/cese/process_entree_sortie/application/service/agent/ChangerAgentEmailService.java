package com.cese.process_entree_sortie.application.service.agent;

import com.cese.process_entree_sortie.application.dto.agent.entree.ChangerEmailCommand;
import com.cese.process_entree_sortie.application.dto.agent.sortie.AgentDTO;
import com.cese.process_entree_sortie.application.port.in.agent.ChangerAgentEmailApi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentPersonnel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class ChangerAgentEmailService implements ChangerAgentEmailApi {
    
    private final AgentSpi agentSpi;
    
    public ChangerAgentEmailService(AgentSpi agentSpi) {
        this.agentSpi = agentSpi;
    }
    
    @Override
    public AgentDTO changerAgentEmail(ChangerEmailCommand command) {
        AgentPersonnel agentExistant = agentSpi.findById(command.agentId())
            .orElseThrow(() -> new RuntimeException("Agent non trouvé avec l'ID: " + command.agentId()));

        agentSpi.findByEmail(command.newEmail())
            .filter(agent -> !agent.id().equals(command.agentId()))
            .ifPresent(agent -> {
                if (agent.etatAgent() == com.cese.process_entree_sortie.domain.utils.ValueObject.EtatAgent.ancien) {
                    agentSpi.save(agent.changerEtatAgent(com.cese.process_entree_sortie.domain.utils.ValueObject.EtatAgent.ancien));
                } else {
                    throw new RuntimeException("Adresse email déjà utilisée par un agent actif.");
                }
            });
        
        AgentPersonnel agentModifie = agentExistant.changerAdresseMail(command.newEmail());
        
        AgentPersonnel agentSauvegarde = agentSpi.save(agentModifie);
        
        return AgentConverter.convertirEnDTO(agentSauvegarde);
    }
}