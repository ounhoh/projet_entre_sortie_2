package com.cese.process_entree_sortie.application.service.template.processus;

import com.cese.process_entree_sortie.application.port.in.template.processus.DesactivateTemplateProcessusApi;
import com.cese.process_entree_sortie.application.port.out.processus.TemplateProcessusSpi;
import com.cese.process_entree_sortie.domain.Processus.model.TemplateProcessus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DesactivateTemplateProcessusService implements DesactivateTemplateProcessusApi {
    
    private final TemplateProcessusSpi templateProcessusSpi;
    
    public DesactivateTemplateProcessusService(TemplateProcessusSpi templateProcessusSpi) {
        this.templateProcessusSpi = templateProcessusSpi;
    }
    
    @Override
    public TemplateProcessus desactivateProcessus(UUID templateId) {
        TemplateProcessus template = templateProcessusSpi.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template processus non trouvé avec l'ID: " + templateId));
        
        TemplateProcessus templateInactif = template.rendreProcessInactif();
        return templateProcessusSpi.save(templateInactif);
    }
}
