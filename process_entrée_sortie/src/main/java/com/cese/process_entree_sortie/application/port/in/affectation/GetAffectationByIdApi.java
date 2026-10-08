package com.cese.process_entree_sortie.application.port.in.affectation;

import com.cese.process_entree_sortie.application.dto.affectation.sortie.AgentAffectationDTO;

import java.util.UUID;

public interface GetAffectationByIdApi {
    AgentAffectationDTO getAffectationById(UUID affectationId);
}
