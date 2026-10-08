package com.cese.process_entree_sortie.application.service.template.groupe_tache;

import com.cese.process_entree_sortie.application.port.in.template.groupe_tache.DeleteTemplateGroupeTacheApi;
import com.cese.process_entree_sortie.application.port.out.tache.TemplateGroupeTacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateGroupeTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeleteTemplateGroupeTacheService implements DeleteTemplateGroupeTacheApi {
    
    private final TemplateGroupeTacheSpi templateGroupeTacheSpi;
    
    public DeleteTemplateGroupeTacheService(TemplateGroupeTacheSpi templateGroupeTacheSpi) {
        this.templateGroupeTacheSpi = templateGroupeTacheSpi;
    }
    
    @Override
    public void deleteGroupeTache(UUID groupeTacheId) {
        TemplateGroupeTache groupe = templateGroupeTacheSpi.findById(groupeTacheId)
            .orElseThrow(() -> new RuntimeException("Template groupe tache non trouvé avec l'ID: " + groupeTacheId));
        templateGroupeTacheSpi.delete(groupe);
    }
}
