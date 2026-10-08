package com.cese.process_entree_sortie.application.service.agent;

import com.cese.process_entree_sortie.application.port.in.agent.GetAgentMaterielApi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentMaterielSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentMaterielEtDroit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetAgentMaterielService implements GetAgentMaterielApi {
    
    private final AgentMaterielSpi agentMaterielSpi;
    
    public GetAgentMaterielService(AgentMaterielSpi agentMaterielSpi) {
        this.agentMaterielSpi = agentMaterielSpi;
    }
    
    @Override
    public Optional<AgentMaterielEtDroit> getAgentMateriel(UUID agentId) {
        return agentMaterielSpi.findByAgentId(agentId);
    }
}
