package com.cese.process_entree_sortie.application.dto.template.sortie;

import com.cese.process_entree_sortie.domain.utils.ValueObject.TacheType;

import java.util.List;
import java.util.UUID;

public record TemplateDependanceDTO(UUID id, UUID sourceTacheId,
                                    List<UUID> cibleTacheIds) {

}
