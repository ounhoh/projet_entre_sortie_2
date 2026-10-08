package com.cese.process_entree_sortie.application.service.agent;

import com.cese.process_entree_sortie.application.dto.agent.entree.CreateAgentCommand;
import com.cese.process_entree_sortie.application.dto.agent.sortie.AgentDTO;
import com.cese.process_entree_sortie.application.port.in.agent.CreateAgentApi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentPersonnel;
import com.cese.process_entree_sortie.domain.Agent.model.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;

@Service
@Transactional
public class CreateAgentService implements CreateAgentApi {
    
    private final AgentSpi agentSpi;
    
    public CreateAgentService(AgentSpi agentSpi) {
        this.agentSpi = agentSpi;
    }
    
    @Override
    public AgentDTO createAgent(CreateAgentCommand command) {
        // Le constructeur avec 7 paramètres met automatiquement etatAgent.entree par défaut
        AgentPersonnel agent = new AgentPersonnel(
            command.agentId(),
            command.nom(),
            command.prenom(),
            Role.valueOf(command.role()),
            new ArrayList<>(), // agentDirections vide au départ
            new ArrayList<>(), // agentAffectations vide au départ
            null // agentMaterielEtDroit null au départ
        );
        
        AgentPersonnel agentSauvegarde = agentSpi.save(agent);
        
        return AgentConverter.convertirEnDTO(agentSauvegarde);
    }
}