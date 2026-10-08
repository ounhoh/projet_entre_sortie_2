package com.cese.process_entree_sortie.application.port.in.affectation;

import com.cese.process_entree_sortie.application.dto.affectation.sortie.AgentAffectationDTO;

import java.util.List;
import java.util.UUID;

// retourne toute les affectations d'un agent
public interface GetHistoriqueAffectationsApi {
    List<AgentAffectationDTO> getHistoriqueAffectation(UUID agentId);
}
