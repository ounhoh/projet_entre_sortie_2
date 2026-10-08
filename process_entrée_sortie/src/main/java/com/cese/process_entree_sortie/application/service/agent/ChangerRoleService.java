package com.cese.process_entree_sortie.application.service.agent;

import com.cese.process_entree_sortie.application.dto.agent.entree.ChangerRoleCommand;
import com.cese.process_entree_sortie.application.dto.agent.sortie.AgentDTO;
import com.cese.process_entree_sortie.application.port.in.agent.ChangerRoleApi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentPersonnel;
import com.cese.process_entree_sortie.domain.Agent.model.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ChangerRoleService implements ChangerRoleApi {
    
    private final AgentSpi agentSpi;
    
    public ChangerRoleService(AgentSpi agentSpi) {
        this.agentSpi = agentSpi;
    }
    
    @Override
    public AgentDTO changerRole(ChangerRoleCommand command) {
        AgentPersonnel agentExistant = agentSpi.findById(command.agentId())
            .orElseThrow(() -> new RuntimeException("Agent non trouvé avec l'ID: " + command.agentId()));
        
        AgentPersonnel agentModifie = agentExistant.changerRole(Role.valueOf(command.newRole()));
        
        AgentPersonnel agentSauvegarde = agentSpi.save(agentModifie);
        
        return AgentConverter.convertirEnDTO(agentSauvegarde);
    }
}