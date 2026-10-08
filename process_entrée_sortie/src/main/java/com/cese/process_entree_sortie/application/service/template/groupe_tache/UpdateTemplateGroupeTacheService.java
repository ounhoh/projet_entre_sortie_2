package com.cese.process_entree_sortie.application.service.template.groupe_tache;

import com.cese.process_entree_sortie.application.dto.template.entree.groupe_tache.UpdateTemplateGroupeTacheCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateGroupeTacheDTO;
import com.cese.process_entree_sortie.application.port.in.template.groupe_tache.UpdateTemplateGroupeTacheApi;
import com.cese.process_entree_sortie.application.port.out.tache.TemplateGroupeTacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateGroupeTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateTemplateGroupeTacheService implements UpdateTemplateGroupeTacheApi {
    
    private final TemplateGroupeTacheSpi templateGroupeTacheSpi;
    private final TemplateGroupeTacheConverter converter;
    
    public UpdateTemplateGroupeTacheService(TemplateGroupeTacheSpi templateGroupeTacheSpi, TemplateGroupeTacheConverter converter) {
        this.templateGroupeTacheSpi = templateGroupeTacheSpi;
        this.converter = converter;
    }
    
    @Override
    public TemplateGroupeTacheDTO updateGroupeTache(UpdateTemplateGroupeTacheCommand command) {
        TemplateGroupeTache groupe = templateGroupeTacheSpi.findById(command.groupeTacheId())
            .orElseThrow(() -> new RuntimeException("Template groupe tache non trouvé avec l'ID: " + command.groupeTacheId()));
        
        TemplateGroupeTache.Builder builder = new TemplateGroupeTache.Builder(groupe);
        if (command.libelle() != null) {
            builder.withlibGroupTache(command.libelle());
        }
        if (command.directionId() != null) {
            builder.withDirectionId(command.directionId());
        }
        
        TemplateGroupeTache groupeModifie = builder.build();
        TemplateGroupeTache groupeSauvegarde = templateGroupeTacheSpi.save(groupeModifie);
        return converter.convertirEnDTO(groupeSauvegarde);
    }
}
