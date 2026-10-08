package com.cese.process_entree_sortie.application.service.template.groupe_tache;

import com.cese.process_entree_sortie.application.dto.template.entree.groupe_tache.RemoveTacheFromTemplateGroupeTacheCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateGroupeTacheDTO;
import com.cese.process_entree_sortie.application.port.in.template.groupe_tache.RemoveTacheFromTemplateGroupeTacheApi;
import com.cese.process_entree_sortie.application.port.out.tache.TemplateGroupeTacheSpi;
import com.cese.process_entree_sortie.application.port.out.tache.TemplateTacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateGroupeTache;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RemoveTacheFromTemplateGroupeTacheService implements RemoveTacheFromTemplateGroupeTacheApi {
    
    private final TemplateGroupeTacheSpi templateGroupeTacheSpi;
    private final TemplateTacheSpi templateTacheSpi;
    private final TemplateGroupeTacheConverter converter;
    
    public RemoveTacheFromTemplateGroupeTacheService(TemplateGroupeTacheSpi templateGroupeTacheSpi,
                                                     TemplateTacheSpi templateTacheSpi,
                                                     TemplateGroupeTacheConverter converter) {
        this.templateGroupeTacheSpi = templateGroupeTacheSpi;
        this.templateTacheSpi = templateTacheSpi;
        this.converter = converter;
    }
    
    @Override
    public TemplateGroupeTacheDTO removeTacheFromGroupeTache(RemoveTacheFromTemplateGroupeTacheCommand command) {
        TemplateGroupeTache groupe = templateGroupeTacheSpi.findById(command.groupeId())
            .orElseThrow(() -> new RuntimeException("Template groupe tache non trouvé avec l'ID: " + command.groupeId()));
        
        TemplateTache tache = templateTacheSpi.findById(command.tacheId())
            .orElseThrow(() -> new RuntimeException("Template tache non trouvée avec l'ID: " + command.tacheId()));
        
        // TODO: Retirer la tache du groupe - nécessite peut-être une mise à jour de la tache ou une table d'association
        // Pour l'instant, on retourne le groupe tel quel
        return converter.convertirEnDTO(groupe);
    }
}
