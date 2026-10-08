package com.cese.process_entree_sortie.application.service.template.processus;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateProcessusDTO;
import com.cese.process_entree_sortie.application.port.in.template.processus.GetTemplatesProcesssusByTypeApi;
import com.cese.process_entree_sortie.application.port.out.processus.TemplateProcessusSpi;
import com.cese.process_entree_sortie.domain.Processus.model.TemplateProcessus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetTemplatesProcesssusByTypeService implements GetTemplatesProcesssusByTypeApi {
    
    private final TemplateProcessusSpi templateProcessusSpi;
    private final TemplateProcessusConverter converter;
    
    public GetTemplatesProcesssusByTypeService(TemplateProcessusSpi templateProcessusSpi, TemplateProcessusConverter converter) {
        this.templateProcessusSpi = templateProcessusSpi;
        this.converter = converter;
    }
    
    @Override
    public List<TemplateProcessusDTO> getTemplateProcessusByType(String type) {
        List<TemplateProcessus> templates = templateProcessusSpi.findByType(type);
        return templates.stream()
            .map(converter::convertirEnDTO)
            .collect(Collectors.toList());
    }
}
