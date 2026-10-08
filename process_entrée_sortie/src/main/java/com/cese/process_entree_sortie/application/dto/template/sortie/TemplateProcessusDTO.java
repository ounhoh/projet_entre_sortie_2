package com.cese.process_entree_sortie.application.dto.template.sortie;

import com.cese.process_entree_sortie.application.dto.processus.sortie.StatutProcessusDTO;

import java.util.UUID;
import java.util.List;

public record TemplateProcessusDTO(UUID id, String code, String libelle,
                                   List<StatutProcessusDTO> statutProcessusDTOList,
                                   List<TemplateGroupeTacheDTO> groupeTacheDTOS) {
}
