package com.cese.process_entree_sortie.application.service.template.processus;

import com.cese.process_entree_sortie.application.dto.template.entree.processus.ListTemplateQuery;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateProcessusDTO;
import com.cese.process_entree_sortie.application.port.in.template.processus.ListTemplateProcessusApi;
import com.cese.process_entree_sortie.application.port.out.processus.TemplateProcessusSpi;
import com.cese.process_entree_sortie.domain.Processus.model.TemplateProcessus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ListTemplateProcessusService implements ListTemplateProcessusApi {
    
    private final TemplateProcessusSpi templateProcessusSpi;
    private final TemplateProcessusConverter converter;
    
    public ListTemplateProcessusService(TemplateProcessusSpi templateProcessusSpi, TemplateProcessusConverter converter) {
        this.templateProcessusSpi = templateProcessusSpi;
        this.converter = converter;
    }
    
    @Override
    public List<TemplateProcessusDTO> getListTemplateProcessus(ListTemplateQuery query) {
        List<TemplateProcessus> templates;
        if (query.actives()) {
            templates = templateProcessusSpi.findActifs();
        } else {
            templates = templateProcessusSpi.findAll();
        }
        
        if (query.type() != null && !query.type().isEmpty()) {
            templates = templates.stream()
                .filter(t -> t.type().toString().equalsIgnoreCase(query.type()))
                .collect(Collectors.toList());
        }
        
        return templates.stream()
            .map(converter::convertirEnDTO)
            .collect(Collectors.toList());
    }
}
