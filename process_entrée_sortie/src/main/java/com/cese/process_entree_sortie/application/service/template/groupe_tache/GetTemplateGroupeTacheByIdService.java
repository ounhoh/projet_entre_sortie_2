package com.cese.process_entree_sortie.application.service.template.groupe_tache;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateGroupeTacheDTO;
import com.cese.process_entree_sortie.application.port.in.template.groupe_tache.GetTemplateGroupeTacheByIdApi;
import com.cese.process_entree_sortie.application.port.out.tache.TemplateGroupeTacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateGroupeTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetTemplateGroupeTacheByIdService implements GetTemplateGroupeTacheByIdApi {
    
    private final TemplateGroupeTacheSpi templateGroupeTacheSpi;
    private final TemplateGroupeTacheConverter converter;
    
    public GetTemplateGroupeTacheByIdService(TemplateGroupeTacheSpi templateGroupeTacheSpi, TemplateGroupeTacheConverter converter) {
        this.templateGroupeTacheSpi = templateGroupeTacheSpi;
        this.converter = converter;
    }
    
    @Override
    public TemplateGroupeTacheDTO getTemplateGroupeTacheById(UUID groupeId) {
        TemplateGroupeTache groupe = templateGroupeTacheSpi.findById(groupeId)
            .orElseThrow(() -> new RuntimeException("Template groupe tache non trouvé avec l'ID: " + groupeId));
        return converter.convertirEnDTO(groupe);
    }
}
