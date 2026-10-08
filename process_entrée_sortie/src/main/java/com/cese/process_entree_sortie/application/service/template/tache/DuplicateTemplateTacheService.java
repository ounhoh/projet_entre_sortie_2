package com.cese.process_entree_sortie.application.service.template.tache;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateTacheDTO;
import com.cese.process_entree_sortie.application.port.in.template.tache.DuplicateTemplateTacheApi;
import com.cese.process_entree_sortie.application.port.out.tache.TemplateTacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DuplicateTemplateTacheService implements DuplicateTemplateTacheApi {
    
    private final TemplateTacheSpi templateTacheSpi;
    private final TemplateTacheConverter converter;
    
    public DuplicateTemplateTacheService(TemplateTacheSpi templateTacheSpi, TemplateTacheConverter converter) {
        this.templateTacheSpi = templateTacheSpi;
        this.converter = converter;
    }
    
    @Override
    public TemplateTacheDTO duplicateTempalteTache(UUID tacheId) {
        TemplateTache tacheOriginale = templateTacheSpi.findById(tacheId)
            .orElseThrow(() -> new RuntimeException("Template tache non trouvée avec l'ID: " + tacheId));
        
        // Créer une copie avec un nouvel ID
        TemplateTache tacheDupliquee = new TemplateTache(
            UUID.randomUUID(),
            tacheOriginale.code() + "_copy",
            tacheOriginale.libelle(),
            tacheOriginale.description(),
            tacheOriginale.type(),
            tacheOriginale.delaijour(),
            tacheOriginale.contenu(),
            tacheOriginale.dependanceId()
        );
        
        TemplateTache tacheSauvegarde = templateTacheSpi.save(tacheDupliquee);
        return converter.convertirEnDTO(tacheSauvegarde);
    }
}
