package com.cese.process_entree_sortie.application.service.dependance;

import com.cese.process_entree_sortie.application.dto.tache.sortie.DependanceDTO;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceDependance;
import org.springframework.stereotype.Component;

@Component
public class DependanceConverter {
    
    /**
     * Convertit une InstanceDependance en DependanceDTO.
     * Nouvelle structure : 1 source → N cibles (uniquement entre tâches).
     */
    public DependanceDTO convertirEnDTO(InstanceDependance dependance) {
        return new DependanceDTO(
            dependance.id(),
            dependance.sourceTacheId(),
            dependance.cibleTacheIds()
        );
    }
}
