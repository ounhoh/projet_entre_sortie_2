package com.cese.process_entree_sortie.application.service.template.groupe_tache;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateGroupeTacheDTO;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateTacheDTO;
import com.cese.process_entree_sortie.application.port.out.tache.TemplateTacheSpi;
import com.cese.process_entree_sortie.application.port.out.direction.DirectionSpi;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateGroupeTache;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TemplateGroupeTacheConverter {
    
    private final TemplateTacheSpi templateTacheSpi;
    private final com.cese.process_entree_sortie.application.service.template.tache.TemplateTacheConverter tacheConverter;
    private final DirectionSpi directionSpi;
    
    public TemplateGroupeTacheConverter(TemplateTacheSpi templateTacheSpi, 
                                       com.cese.process_entree_sortie.application.service.template.tache.TemplateTacheConverter tacheConverter,
                                       DirectionSpi directionSpi) {
        this.templateTacheSpi = templateTacheSpi;
        this.tacheConverter = tacheConverter;
        this.directionSpi = directionSpi;
    }
    
    public TemplateGroupeTacheDTO convertirEnDTO(TemplateGroupeTache groupe) {
        // Récupérer les tâches du groupe
        List<TemplateTacheDTO> tachesDTO = new ArrayList<>();
        List<com.cese.process_entree_sortie.domain.Tache.model.TemplateTache> taches = templateTacheSpi.findByTemplateGroupeId(groupe.id());
        if (taches != null) {
            tachesDTO = taches.stream()
                .map(tacheConverter::convertirEnDTO)
                .collect(Collectors.toList());
        }
        
        // Récupérer le libellé de la direction
        String libelleDirection = null;
        if (groupe.directionId() != null) {
            libelleDirection = directionSpi.findById(groupe.directionId())
                .map(dir -> dir.libDirection())
                .orElse(null);
        }
        
        return new TemplateGroupeTacheDTO(
            groupe.id(),
            groupe.codeTemplate(),
            groupe.libGroupTache(),
            groupe.statutProcessusId(),
            groupe.codeDirection(),
            libelleDirection,
            groupe.isDirectionConcernee(),
            tachesDTO
        );
    }
}
