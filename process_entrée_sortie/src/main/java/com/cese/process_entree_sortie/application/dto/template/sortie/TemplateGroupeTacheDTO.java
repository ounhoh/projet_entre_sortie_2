package com.cese.process_entree_sortie.application.dto.template.sortie;

import java.util.UUID;
import java.util.List;

public record TemplateGroupeTacheDTO(UUID id, String code, String libelle,
                                     UUID statutProcessusId,
                                     String codeDirection,
                                     String libelleDirection,
                                     boolean isDirectionConcernee,
                                     List<TemplateTacheDTO> tacheDTOList) {
}
