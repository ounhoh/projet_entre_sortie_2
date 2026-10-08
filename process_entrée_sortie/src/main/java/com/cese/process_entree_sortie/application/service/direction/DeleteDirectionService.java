package com.cese.process_entree_sortie.application.service.direction;

import com.cese.process_entree_sortie.application.port.in.agent.DeleteAgentApi;
import com.cese.process_entree_sortie.application.port.in.direction.DeleteDirectionApi;
import com.cese.process_entree_sortie.application.port.out.agent.AgentDirectionSpi;
import com.cese.process_entree_sortie.application.port.out.direction.DirectionSpi;
import com.cese.process_entree_sortie.domain.Direction.model.Direction;
import com.cese.process_entree_sortie.domain.utils.error.DirectionNonTrouveeException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class DeleteDirectionService implements DeleteDirectionApi {
    
    private final DirectionSpi directionSpi;
    private final AgentDirectionSpi agentDirectionSpi;
    private final DeleteAgentApi deleteAgentApi;
    
    public DeleteDirectionService(
            DirectionSpi directionSpi,
            AgentDirectionSpi agentDirectionSpi,
            DeleteAgentApi deleteAgentApi) {
        this.directionSpi = directionSpi;
        this.agentDirectionSpi = agentDirectionSpi;
        this.deleteAgentApi = deleteAgentApi;
    }
    
    @Override
    public void deleteDirection(UUID directionId) {
        Direction direction = directionSpi.findById(directionId)
            .orElseThrow(() -> new DirectionNonTrouveeException("Direction non trouvée avec l'ID: " + directionId));
        
        // Supprimer les agents liés à cette direction (et leurs processus)
        Set<UUID> agentIds = new HashSet<>();
        agentDirectionSpi.findByDirectionId(directionId).forEach(ad -> {
            if (ad.agentPersonnelId() != null) {
                agentIds.add(ad.agentPersonnelId());
            }
        });
        for (UUID agentId : agentIds) {
            deleteAgentApi.deleteAgent(agentId);
        }
        
        directionSpi.delete(direction);
    }
}