package com.cese.process_entree_sortie.application.service.template.processus;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateProcessusDTO;
import com.cese.process_entree_sortie.application.port.in.template.processus.DuplicateTemplateProcessusApi;
import com.cese.process_entree_sortie.application.port.out.processus.TemplateProcessusSpi;
import com.cese.process_entree_sortie.domain.Processus.model.TemplateProcessus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DuplicateTemplateProcessusService implements DuplicateTemplateProcessusApi {
    
    private final TemplateProcessusSpi templateProcessusSpi;
    private final TemplateProcessusConverter converter;
    
    public DuplicateTemplateProcessusService(TemplateProcessusSpi templateProcessusSpi, TemplateProcessusConverter converter) {
        this.templateProcessusSpi = templateProcessusSpi;
        this.converter = converter;
    }
    
    @Override
    public TemplateProcessusDTO duplicateTemplateProcesus(UUID templateId) {
        TemplateProcessus templateOriginal = templateProcessusSpi.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template processus non trouvé avec l'ID: " + templateId));
        
        // Créer une copie avec un nouvel ID
        TemplateProcessus templateDuplique = TemplateProcessus.builder(templateOriginal)
            .withId(UUID.randomUUID())
            .withCodeProcess(templateOriginal.codeProcessus() + "_copy")
            .withEtatActif(false) // Dupliqué inactif par défaut
            .build();
        
        TemplateProcessus templateSauvegarde = templateProcessusSpi.save(templateDuplique);
        return converter.convertirEnDTO(templateSauvegarde);
    }
}
