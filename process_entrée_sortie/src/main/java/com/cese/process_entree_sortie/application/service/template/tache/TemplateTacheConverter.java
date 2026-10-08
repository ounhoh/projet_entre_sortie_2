package com.cese.process_entree_sortie.application.service.template.tache;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateTacheDTO;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateTache;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TemplateTacheConverter {
    
    public TemplateTacheDTO convertirEnDTO(TemplateTache tache) {
        Map<String, Object> contenu = Map.of();
        if (tache.contenu() != null && tache.contenu().contenuTache() != null && !tache.contenu().contenuTache().isEmpty()) {
            contenu = tache.contenu().contenuTache();
        }
        
        return new TemplateTacheDTO(
            tache.id(),
            tache.libelle(),
            tache.code(),
            tache.description(),
            contenu
        );
    }
}
