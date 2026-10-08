package com.cese.process_entree_sortie.application.service.template.groupe_tache;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateGroupeTacheDTO;
import com.cese.process_entree_sortie.application.port.in.template.groupe_tache.ListGroupsTacheByTemplateProcessusApi;
import com.cese.process_entree_sortie.application.port.out.tache.TemplateGroupeTacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateGroupeTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ListGroupsTacheByTemplateProcessusService implements ListGroupsTacheByTemplateProcessusApi {
    
    private final TemplateGroupeTacheSpi templateGroupeTacheSpi;
    private final TemplateGroupeTacheConverter converter;
    
    public ListGroupsTacheByTemplateProcessusService(TemplateGroupeTacheSpi templateGroupeTacheSpi, TemplateGroupeTacheConverter converter) {
        this.templateGroupeTacheSpi = templateGroupeTacheSpi;
        this.converter = converter;
    }
    
    @Override
    public List<TemplateGroupeTacheDTO> getListGroupTacheByTemplateProcessus(UUID templateProcessusId) {
        List<TemplateGroupeTache> groupes = templateGroupeTacheSpi.findByTemplateProcessusId(templateProcessusId);
        return groupes.stream()
            .map(converter::convertirEnDTO)
            .collect(Collectors.toList());
    }
}
