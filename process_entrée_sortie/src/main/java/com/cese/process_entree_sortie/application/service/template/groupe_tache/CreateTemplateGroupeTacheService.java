package com.cese.process_entree_sortie.application.service.template.groupe_tache;

import com.cese.process_entree_sortie.application.dto.template.entree.groupe_tache.CreateTemplateGroupeTacheCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateGroupeTacheDTO;
import com.cese.process_entree_sortie.application.port.in.template.groupe_tache.CreateTemplateGroupeTacheApi;
import com.cese.process_entree_sortie.application.port.out.tache.TemplateGroupeTacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateGroupeTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CreateTemplateGroupeTacheService implements CreateTemplateGroupeTacheApi {
    
    private final TemplateGroupeTacheSpi templateGroupeTacheSpi;
    private final TemplateGroupeTacheConverter converter;
    
    public CreateTemplateGroupeTacheService(TemplateGroupeTacheSpi templateGroupeTacheSpi, TemplateGroupeTacheConverter converter) {
        this.templateGroupeTacheSpi = templateGroupeTacheSpi;
        this.converter = converter;
    }
    
    @Override
    public TemplateGroupeTacheDTO createTemplateGroupeTache(CreateTemplateGroupeTacheCommand command) {
        TemplateGroupeTache nouveauGroupe = TemplateGroupeTache.Builder()
            .withId(UUID.randomUUID())
            .withCodeTemplate(command.code())
            .withlibGroupTache(command.libelle())
            .withCodeDirection(command.codeDirection() != null ? command.codeDirection() : "")
            .withDirectionId(command.directionId())
            .withStatutProcessusId(command.statutProcessusId())
            .withTemplateProcessusId(command.templateProcessusId())
            .build();
        
        TemplateGroupeTache groupeSauvegarde = templateGroupeTacheSpi.save(nouveauGroupe);
        return converter.convertirEnDTO(groupeSauvegarde);
    }
}
