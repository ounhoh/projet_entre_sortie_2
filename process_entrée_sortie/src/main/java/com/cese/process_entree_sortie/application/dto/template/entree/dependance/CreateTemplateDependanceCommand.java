package com.cese.process_entree_sortie.application.dto.template.entree.dependance;

import java.util.List;
import java.util.UUID;

public record CreateTemplateDependanceCommand(UUID sourceTacheId,
                                              List<UUID> cibleTacheIds) {
}
