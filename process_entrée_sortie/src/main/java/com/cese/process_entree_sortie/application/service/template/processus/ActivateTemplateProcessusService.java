package com.cese.process_entree_sortie.application.service.template.processus;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateProcessusDTO;
import com.cese.process_entree_sortie.application.port.in.template.processus.ActivateTemplateProcessusApi;
import com.cese.process_entree_sortie.application.port.out.processus.TemplateProcessusSpi;
import com.cese.process_entree_sortie.domain.Processus.model.TemplateProcessus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ActivateTemplateProcessusService implements ActivateTemplateProcessusApi {
    
    private final TemplateProcessusSpi templateProcessusSpi;
    private final TemplateProcessusConverter converter;
    
    public ActivateTemplateProcessusService(TemplateProcessusSpi templateProcessusSpi, TemplateProcessusConverter converter) {
        this.templateProcessusSpi = templateProcessusSpi;
        this.converter = converter;
    }
    
    @Override
    public TemplateProcessusDTO activateTemplateProcessus(UUID templateId) {
        TemplateProcessus template = templateProcessusSpi.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template processus non trouvé avec l'ID: " + templateId));
        
        TemplateProcessus templateActif = template.rendreProcessActif();
        TemplateProcessus templateSauvegarde = templateProcessusSpi.save(templateActif);
        return converter.convertirEnDTO(templateSauvegarde);
    }
}
