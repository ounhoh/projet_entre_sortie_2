package com.cese.process_entree_sortie.application.service.template.processus;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateProcessusDTO;
import com.cese.process_entree_sortie.application.port.in.template.processus.GetTemplateProcessusByIdApi;
import com.cese.process_entree_sortie.application.port.out.processus.TemplateProcessusSpi;
import com.cese.process_entree_sortie.domain.Processus.model.TemplateProcessus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetTemplateProcessusByIdService implements GetTemplateProcessusByIdApi {
    
    private final TemplateProcessusSpi templateProcessusSpi;
    private final TemplateProcessusConverter converter;
    
    public GetTemplateProcessusByIdService(TemplateProcessusSpi templateProcessusSpi, TemplateProcessusConverter converter) {
        this.templateProcessusSpi = templateProcessusSpi;
        this.converter = converter;
    }
    
    @Override
    public TemplateProcessusDTO getTemplateProcessusById(UUID templateId) {
        TemplateProcessus template = templateProcessusSpi.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template processus non trouvé avec l'ID: " + templateId));
        return converter.convertirEnDTO(template);
    }
}
