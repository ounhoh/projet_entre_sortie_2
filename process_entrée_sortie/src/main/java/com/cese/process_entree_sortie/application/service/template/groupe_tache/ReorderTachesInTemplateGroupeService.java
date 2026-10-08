package com.cese.process_entree_sortie.application.service.template.groupe_tache;

import com.cese.process_entree_sortie.application.dto.template.entree.groupe_tache.ReorderTacheInGroupeCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateGroupeTacheDTO;
import com.cese.process_entree_sortie.application.port.in.template.groupe_tache.ReorderTachesInTemplateGroupeApi;
import com.cese.process_entree_sortie.application.port.out.tache.TemplateGroupeTacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateGroupeTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReorderTachesInTemplateGroupeService implements ReorderTachesInTemplateGroupeApi {
    
    private final TemplateGroupeTacheSpi templateGroupeTacheSpi;
    private final TemplateGroupeTacheConverter converter;
    
    public ReorderTachesInTemplateGroupeService(TemplateGroupeTacheSpi templateGroupeTacheSpi, TemplateGroupeTacheConverter converter) {
        this.templateGroupeTacheSpi = templateGroupeTacheSpi;
        this.converter = converter;
    }
    
    @Override
    public TemplateGroupeTacheDTO reoderTacheInTemplateGroupe(ReorderTacheInGroupeCommand command) {
        TemplateGroupeTache groupe = templateGroupeTacheSpi.findById(command.templateGroupeId())
            .orElseThrow(() -> new RuntimeException("Template groupe tache non trouvé avec l'ID: " + command.templateGroupeId()));
        
        // TODO: Réordonner les tâches - nécessite peut-être une table d'association avec un ordre
        // Pour l'instant, on retourne le groupe tel quel
        return converter.convertirEnDTO(groupe);
    }
}
