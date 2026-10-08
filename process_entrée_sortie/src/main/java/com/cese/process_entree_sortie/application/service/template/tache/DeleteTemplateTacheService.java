package com.cese.process_entree_sortie.application.service.template.tache;

import com.cese.process_entree_sortie.application.port.in.template.tache.DeleteTemplateTacheApi;
import com.cese.process_entree_sortie.application.port.out.tache.TemplateTacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeleteTemplateTacheService implements DeleteTemplateTacheApi {
    
    private final TemplateTacheSpi templateTacheSpi;
    
    public DeleteTemplateTacheService(TemplateTacheSpi templateTacheSpi) {
        this.templateTacheSpi = templateTacheSpi;
    }
    
    @Override
    public void deleteTemplateTache(UUID tacheId) {
        TemplateTache tache = templateTacheSpi.findById(tacheId)
            .orElseThrow(() -> new RuntimeException("Template tache non trouvée avec l'ID: " + tacheId));
        templateTacheSpi.delete(tache);
    }
}
