package com.cese.process_entree_sortie.application.dto.tache.entree;

import java.util.UUID;

public record DesassignerAgentToTacheCommand(UUID tacheId, UUID agentId) {
}
