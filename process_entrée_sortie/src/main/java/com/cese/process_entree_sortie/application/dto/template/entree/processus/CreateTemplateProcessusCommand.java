package com.cese.process_entree_sortie.application.dto.template.entree.processus;

import com.cese.process_entree_sortie.application.dto.processus.sortie.StatutProcessusDTO;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateGroupeTacheDTO;

import java.util.List;

public record CreateTemplateProcessusCommand(String code, String libelle,
                                             String type, String description, List<StatutProcessusDTO> statutProcessusDTOList,
                                             List<TemplateGroupeTacheDTO> groupeTacheDTOS) {
}
