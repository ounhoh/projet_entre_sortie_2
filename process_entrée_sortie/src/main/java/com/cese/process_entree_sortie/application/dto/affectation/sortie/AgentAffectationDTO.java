package com.cese.process_entree_sortie.application.dto.affectation.sortie;

import java.util.UUID;

public record AgentAffectationDTO(
        String fonction,
        UUID agentResponsableId,
        UUID agentAcceuilId,
        UUID directionId,
        UUID agentId
) {
}
