package com.cese.process_entree_sortie.application.dto.template.entree.processus;

import com.cese.process_entree_sortie.application.dto.processus.sortie.StatutProcessusDTO;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateGroupeTacheDTO;

import java.util.UUID;
import java.util.List;

public record UpdateTemplateProcessusCommand(UUID templateId, String libelle, String description,
                                             List<StatutProcessusDTO> statutList,
                                             List<TemplateGroupeTacheDTO> groupeTaches
                                             ) {
}
