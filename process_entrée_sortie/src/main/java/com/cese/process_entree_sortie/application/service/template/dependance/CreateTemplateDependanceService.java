package com.cese.process_entree_sortie.application.service.template.dependance;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateDependanceDTO;
import com.cese.process_entree_sortie.application.port.in.template.dependance.CreateTemplateDependanceApi;
import com.cese.process_entree_sortie.application.port.out.dependance.TemplateDependanceSpi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cese.process_entree_sortie.application.dto.template.entree.dependance.CreateTemplateDependanceCommand;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateDependance;
import java.util.UUID;

@Service
@Transactional
public class CreateTemplateDependanceService implements CreateTemplateDependanceApi {
    
    private final TemplateDependanceSpi templateDependanceSpi;
    private final TemplateDependanceConverter converter;
    
    public CreateTemplateDependanceService(TemplateDependanceSpi templateDependanceSpi, TemplateDependanceConverter converter) {
        this.templateDependanceSpi = templateDependanceSpi;
        this.converter = converter;
    }
    
    @Override
    public TemplateDependanceDTO createTemplateDependance(CreateTemplateDependanceCommand command) {
        TemplateDependance nouvelleDependance = new TemplateDependance(
            UUID.randomUUID(),
            command.sourceTacheId(),
            command.cibleTacheIds()
        );
        TemplateDependance dependanceSauvegarde = templateDependanceSpi.save(nouvelleDependance);
        return converter.convertirEnDTO(dependanceSauvegarde);
    }
}
