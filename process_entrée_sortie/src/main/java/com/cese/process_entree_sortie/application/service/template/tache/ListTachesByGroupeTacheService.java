package com.cese.process_entree_sortie.application.service.template.tache;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateTacheDTO;
import com.cese.process_entree_sortie.application.port.in.template.tache.ListTachesByGroupeTacheApi;
import com.cese.process_entree_sortie.application.port.out.tache.TemplateTacheSpi;
import com.cese.process_entree_sortie.domain.Tache.model.TemplateTache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ListTachesByGroupeTacheService implements ListTachesByGroupeTacheApi {
    
    private final TemplateTacheSpi templateTacheSpi;
    private final TemplateTacheConverter converter;
    
    public ListTachesByGroupeTacheService(TemplateTacheSpi templateTacheSpi, TemplateTacheConverter converter) {
        this.templateTacheSpi = templateTacheSpi;
        this.converter = converter;
    }
    
    @Override
    public List<TemplateTacheDTO> getListTacheByGroupeTache(UUID groupeTacheID) {
        List<TemplateTache> taches = templateTacheSpi.findByTemplateGroupeId(groupeTacheID);
        return taches.stream()
            .map(converter::convertirEnDTO)
            .collect(Collectors.toList());
    }
}
