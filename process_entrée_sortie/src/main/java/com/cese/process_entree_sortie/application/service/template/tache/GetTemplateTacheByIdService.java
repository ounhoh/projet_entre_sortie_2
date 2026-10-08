package com.cese.process_entree_sortie.application.service.template.tache;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateTacheDTO;
import com.cese.process_entree_sortie.application.port.in.template.tache.GetTemplateTacheByIdApi;
import com.cese.process_entree_sortie.application.port.out.tache.TemplateTacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetTemplateTacheByIdService implements GetTemplateTacheByIdApi {
    
    private final TemplateTacheSpi templateTacheSpi;
    private final TemplateTacheConverter converter;
    
    public GetTemplateTacheByIdService(TemplateTacheSpi templateTacheSpi, TemplateTacheConverter converter) {
        this.templateTacheSpi = templateTacheSpi;
        this.converter = converter;
    }
    
    @Override
    public TemplateTacheDTO getTemplateTacheById(UUID tacheId) {
        TemplateTache tache = templateTacheSpi.findById(tacheId)
            .orElseThrow(() -> new RuntimeException("Template tache non trouvée avec l'ID: " + tacheId));
        return converter.convertirEnDTO(tache);
    }
}
