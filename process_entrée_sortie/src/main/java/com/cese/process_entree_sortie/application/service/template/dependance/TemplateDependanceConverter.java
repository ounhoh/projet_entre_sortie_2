package com.cese.process_entree_sortie.application.service.template.dependance;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateDependanceDTO;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateDependance;
import org.springframework.stereotype.Component;


@Component
public class TemplateDependanceConverter {
    
    public TemplateDependanceDTO convertirEnDTO(TemplateDependance dependance) {
        // TemplateDependance utilise sourceId (un seul champ) et cibleTacheId/cibleGroupeTacheId
        return new TemplateDependanceDTO(
            dependance.id(),
            dependance.sourceTacheId(),
            dependance.cibleTacheIds()
        );
    }
}
