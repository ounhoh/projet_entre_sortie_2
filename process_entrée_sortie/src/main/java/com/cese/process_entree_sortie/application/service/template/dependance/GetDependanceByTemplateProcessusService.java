package com.cese.process_entree_sortie.application.service.template.dependance;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateDependanceDTO;
import com.cese.process_entree_sortie.application.port.in.template.dependance.GetDependanceByTemplateProcessusApi;
import com.cese.process_entree_sortie.application.port.out.dependance.TemplateDependanceSpi;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateDependance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetDependanceByTemplateProcessusService implements GetDependanceByTemplateProcessusApi {
    
    private final TemplateDependanceSpi templateDependanceSpi;
    private final TemplateDependanceConverter converter;
    
    public GetDependanceByTemplateProcessusService(TemplateDependanceSpi templateDependanceSpi, TemplateDependanceConverter converter) {
        this.templateDependanceSpi = templateDependanceSpi;
        this.converter = converter;
    }
    
    @Override
    public List<TemplateDependanceDTO> getDependanceByTemplateProcessus(UUID templateProcessusId) {
        List<TemplateDependance> dependances = templateDependanceSpi.findByTemplateProcessusId(templateProcessusId);
        return dependances.stream()
            .map(converter::convertirEnDTO)
            .collect(Collectors.toList());
    }
}
