package com.cese.process_entree_sortie.application.dto.affectation.entree;

import java.util.UUID;

public record AssignerResponsableCommand(UUID affectationId, UUID agentResponsable) {
}
