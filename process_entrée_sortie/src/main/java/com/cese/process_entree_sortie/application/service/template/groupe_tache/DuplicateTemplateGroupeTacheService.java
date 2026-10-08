package com.cese.process_entree_sortie.application.service.template.groupe_tache;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateGroupeTacheDTO;
import com.cese.process_entree_sortie.application.port.in.template.groupe_tache.DuplicateTemplateGroupeTacheApi;
import com.cese.process_entree_sortie.application.port.out.tache.TemplateGroupeTacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateGroupeTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DuplicateTemplateGroupeTacheService implements DuplicateTemplateGroupeTacheApi {
    
    private final TemplateGroupeTacheSpi templateGroupeTacheSpi;
    private final TemplateGroupeTacheConverter converter;
    
    public DuplicateTemplateGroupeTacheService(TemplateGroupeTacheSpi templateGroupeTacheSpi, TemplateGroupeTacheConverter converter) {
        this.templateGroupeTacheSpi = templateGroupeTacheSpi;
        this.converter = converter;
    }
    
    @Override
    public TemplateGroupeTacheDTO duplicateTemplateGroupeTache(UUID groupeId) {
        TemplateGroupeTache groupeOriginal = templateGroupeTacheSpi.findById(groupeId)
            .orElseThrow(() -> new RuntimeException("Template groupe tache non trouvé avec l'ID: " + groupeId));
        
        TemplateGroupeTache.Builder builder = new TemplateGroupeTache.Builder(groupeOriginal);
        TemplateGroupeTache groupeDuplique = builder
            .withId(UUID.randomUUID())
            .withCodeTemplate(groupeOriginal.codeTemplate() + "_copy")
            .build();
        
        TemplateGroupeTache groupeSauvegarde = templateGroupeTacheSpi.save(groupeDuplique);
        return converter.convertirEnDTO(groupeSauvegarde);
    }
}
