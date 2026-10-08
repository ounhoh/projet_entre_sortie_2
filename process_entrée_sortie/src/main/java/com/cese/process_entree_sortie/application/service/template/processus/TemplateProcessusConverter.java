package com.cese.process_entree_sortie.application.service.template.processus;

import com.cese.process_entree_sortie.application.dto.processus.sortie.StatutProcessusDTO;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateGroupeTacheDTO;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateProcessusDTO;
import com.cese.process_entree_sortie.application.service.template.groupe_tache.TemplateGroupeTacheConverter;
import com.cese.process_entree_sortie.domain.Processus.model.StatutProcessus;
import com.cese.process_entree_sortie.domain.Processus.model.TemplateProcessus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TemplateProcessusConverter {
    
    private final TemplateGroupeTacheConverter groupeTacheConverter;
    
    public TemplateProcessusConverter(TemplateGroupeTacheConverter groupeTacheConverter) {
        this.groupeTacheConverter = groupeTacheConverter;
    }
    
    public TemplateProcessusDTO convertirEnDTO(TemplateProcessus template) {
        // Convertir les statuts
        List<StatutProcessusDTO> statutsDTO = new ArrayList<>();
        if (template.statusList() != null) {
            for (StatutProcessus statut : template.statusList()) {
                statutsDTO.add(new StatutProcessusDTO(statut.id(), statut.codeStatut(), statut.libStatut()));
            }
        }
        
        // Convertir les groupes de tâches
        List<TemplateGroupeTacheDTO> groupesDTO = new ArrayList<>();
        if (template.groupeTacheList() != null && !template.groupeTacheList().isEmpty()) {
            groupesDTO = template.groupeTacheList().stream()
                .map(groupeTacheConverter::convertirEnDTO)
                .collect(Collectors.toList());
        }
        
        return new TemplateProcessusDTO(
            template.id(),
            template.codeProcessus(),
            template.libProcessus(),
            statutsDTO,
            groupesDTO
        );
    }
}
