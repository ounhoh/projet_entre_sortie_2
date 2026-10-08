package com.cese.process_entree_sortie.application.service.agent;

import com.cese.process_entree_sortie.application.dto.agent.sortie.AgentDTO;
import com.cese.process_entree_sortie.application.port.out.direction.DirectionSpi;
import com.cese.process_entree_sortie.domain.Agent.model.AgentPersonnel;
import com.cese.process_entree_sortie.domain.Agent.model.AgentDirection;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public class AgentConverter {
    
    /**
     * Convertit un AgentPersonnel en AgentDTO avec la date de sortie
     * Version sans DirectionSpi (pour compatibilité)
     */
    public static AgentDTO convertirEnDTO(AgentPersonnel agent) {
        return convertirEnDTO(agent, null);
    }
    
    /**
     * Convertit un AgentPersonnel en AgentDTO avec la date de sortie et la direction
     * @param agent L'agent à convertir
     * @param directionSpi Le SPI pour récupérer les directions (optionnel)
     */
    public static AgentDTO convertirEnDTO(AgentPersonnel agent, DirectionSpi directionSpi) {
        String code = "";
        String libelle = "";
        String direction = "";
        UUID agentResponsable = null;
        LocalDate dateArrivee = null;
        LocalDate dateSortie = null;
        String numeroBureau = null;
        if (agent.agentAffectations() != null && !agent.agentAffectations().isEmpty()) {
            var affectation = agent.agentAffectations().get(0);
            agentResponsable = affectation.agentResponsableId();
        }
        
        // Récupérer la direction actuelle et la date de sortie depuis les directions de l'agent
        if (agent.agentDirections() != null && !agent.agentDirections().isEmpty()) {
            // Trouver la direction actuelle (sans date de départ ou avec date de départ future)
            Optional<AgentDirection> directionActuelleOpt = agent.agentDirections().stream()
                .filter(agentDir -> {
                    Optional<LocalDate> dateDepart = agentDir.getOPtionalDateDepart();
                    return dateDepart.isEmpty() || dateDepart.get().isAfter(LocalDate.now()) || dateDepart.get().equals(LocalDate.now());
                })
                .findFirst();
            
            // Si pas de direction actuelle, prendre la première
            AgentDirection directionActuelle = directionActuelleOpt.orElse(agent.agentDirections().get(0));
            numeroBureau = directionActuelle.numeroBureau();
            // Récupérer le libellé de la direction si DirectionSpi est fourni
            String directionLibelle = "";
            if (directionSpi != null) {
                Optional<String> directionLibelleOpt = directionSpi.findById(directionActuelle.directionId())
                    .map(dir -> dir.libDirection());
                directionLibelle = directionLibelleOpt.orElse(directionActuelle.codeDirection());
            } else {
                // Fallback: utiliser le codeDirection si DirectionSpi n'est pas disponible
                directionLibelle = directionActuelle.codeDirection();
            }
            direction = directionLibelle;
            dateArrivee = directionActuelle.dateArrivee();
            
            // Chercher la première direction avec une date de départ (date de sortie)
            Optional<LocalDate> dateDepartOpt = agent.agentDirections().stream()
                .map(agentDir -> agentDir.getOPtionalDateDepart())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
            
            if (dateDepartOpt.isPresent()) {
                dateSortie = dateDepartOpt.get();
            }
        }
        
        return new AgentDTO(
            agent.id(),
            agent.nom(),
            agent.prenom(),
            agent.email(),
            agent.role(),
            code,
            libelle,
            "",
            direction,
            agentResponsable,
            dateArrivee,
            dateSortie,
            agent.etatAgent(),
            numeroBureau        );
    }
}
