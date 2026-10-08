package com.cese.process_entree_sortie.application.dto.tache.entree;

import java.util.List;
import java.util.UUID;

public record CreateDependanceCommand(UUID sourceTacheId,
                                      List<UUID> cibleTacheIds) {
}
