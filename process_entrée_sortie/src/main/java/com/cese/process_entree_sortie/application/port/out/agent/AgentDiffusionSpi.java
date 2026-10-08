package com.cese.process_entree_sortie.application.port.out.agent;

import com.cese.process_entree_sortie.domain.Agent.model.AgentDiffusion;

import java.util.Optional;
import java.util.UUID;

/**
 * Port de sortie pour gérer les listes de diffusion des agents.
 */
public interface AgentDiffusionSpi {
    
    /**
     * Sauvegarde la liste de diffusion d'un agent.
     * 
     * @param diffusion La liste de diffusion à sauvegarder
     * @return La liste de diffusion sauvegardée
     */
    AgentDiffusion save(AgentDiffusion diffusion);
    
    /**
     * Récupère la liste de diffusion d'un agent.
     * 
     * @param agentId L'ID de l'agent
     * @return La liste de diffusion, ou Optional.empty() si non trouvée
     */
    Optional<AgentDiffusion> findByAgentId(UUID agentId);
    
    /**
     * Supprime la liste de diffusion d'un agent.
     * 
     * @param agentId L'ID de l'agent
     */
    void deleteByAgentId(UUID agentId);
}
