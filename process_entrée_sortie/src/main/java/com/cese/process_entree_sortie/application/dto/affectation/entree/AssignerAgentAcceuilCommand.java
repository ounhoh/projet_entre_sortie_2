package com.cese.process_entree_sortie.application.dto.affectation.entree;

import java.util.UUID;

public record AssignerAgentAcceuilCommand(UUID affectationId, UUID agentAcceuilId) {
}
