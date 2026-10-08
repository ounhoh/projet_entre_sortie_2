package com.cese.process_entree_sortie.application.dto.template.entree.groupe_tache;

import java.util.List;
import java.util.UUID;

public record ReorderTacheInGroupeCommand(UUID templateGroupeId, List<UUID> orderdTacheId) {
}
