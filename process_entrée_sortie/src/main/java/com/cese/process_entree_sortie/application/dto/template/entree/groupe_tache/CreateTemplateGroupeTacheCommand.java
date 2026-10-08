package com.cese.process_entree_sortie.application.dto.template.entree.groupe_tache;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateTacheDTO;

import java.util.List;
import java.util.UUID;

public record CreateTemplateGroupeTacheCommand(String code, String libelle, String codeDirection,
                                               UUID directionId, UUID statutProcessusId, UUID templateProcessusId,
                                               List<TemplateTacheDTO> taches) {
}
