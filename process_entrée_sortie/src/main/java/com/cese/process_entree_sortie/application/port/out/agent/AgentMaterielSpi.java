package com.cese.process_entree_sortie.application.port.out.agent;

import com.cese.process_entree_sortie.domain.Agent.model.AgentMaterielEtDroit;

import java.util.Optional;
import java.util.UUID;

/**
 * Port de sortie pour gérer les matériels et droits des agents.
 */
public interface AgentMaterielSpi {
    
    /**
     * Sauvegarde le matériel et droits d'un agent.
     * 
     * @param agentId L'ID de l'agent
     * @param materiel Le matériel et droits à sauvegarder
     * @return Le matériel sauvegardé
     */
    AgentMaterielEtDroit save(UUID agentId, AgentMaterielEtDroit materiel);
    
    /**
     * Récupère le matériel et droits d'un agent.
     * 
     * @param agentId L'ID de l'agent
     * @return Le matériel et droits, ou Optional.empty() si non trouvé
     */
    Optional<AgentMaterielEtDroit> findByAgentId(UUID agentId);
    
    /**
     * Supprime le matériel et droits d'un agent.
     * 
     * @param agentId L'ID de l'agent
     */
    void deleteByAgentId(UUID agentId);
}
