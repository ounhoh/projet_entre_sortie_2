package com.cese.process_entree_sortie.application.service.template.editor;

import com.cese.process_entree_sortie.application.dto.template.entree.processus.AddGroupeToTemplateCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateProcessusDTO;
import com.cese.process_entree_sortie.application.port.in.template.editor.AddGroupeToTemplateApi;
import com.cese.process_entree_sortie.application.port.out.tache.TemplateGroupeTacheSpi;
import com.cese.process_entree_sortie.application.port.out.processus.TemplateProcessusSpi;
import com.cese.process_entree_sortie.application.service.template.processus.TemplateProcessusConverter;
import com.cese.process_entree_sortie.domain.Processus.model.TemplateProcessus;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateGroupeTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class AddGroupeToTemplateService implements AddGroupeToTemplateApi {
    
    private final TemplateProcessusSpi templateProcessusSpi;
    private final TemplateGroupeTacheSpi templateGroupeTacheSpi;
    private final TemplateProcessusConverter converter;
    
    public AddGroupeToTemplateService(TemplateProcessusSpi templateProcessusSpi, TemplateGroupeTacheSpi templateGroupeTacheSpi, TemplateProcessusConverter converter) {
        this.templateProcessusSpi = templateProcessusSpi;
        this.templateGroupeTacheSpi = templateGroupeTacheSpi;
        this.converter = converter;
    }
    
    @Override
    public TemplateProcessusDTO addGroupeToTemplate(AddGroupeToTemplateCommand command) {
        TemplateProcessus template = templateProcessusSpi.findById(command.templateProcessusId())
            .orElseThrow(() -> new RuntimeException("Template processus non trouvé avec l'ID: " + command.templateProcessusId()));
        
        TemplateGroupeTache groupe = templateGroupeTacheSpi.findById(command.groupeId())
            .orElseThrow(() -> new RuntimeException("Template groupe tache non trouvé avec l'ID: " + command.groupeId()));
        
        // Utiliser le Builder pour ajouter le groupe
        TemplateProcessus templateModifie = TemplateProcessus.builder(template)
            .addGroupeTache(groupe)
            .build();
        
        TemplateProcessus templateSauvegarde = templateProcessusSpi.save(templateModifie);
        return converter.convertirEnDTO(templateSauvegarde);
    }
}
