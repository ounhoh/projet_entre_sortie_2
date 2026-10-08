package com.cese.process_entree_sortie.application.port.in.affectation;

import com.cese.process_entree_sortie.application.dto.affectation.entree.CreateAffectationCommand;
import com.cese.process_entree_sortie.application.dto.affectation.sortie.AgentAffectationDTO;

public interface CreateAffectationApi {
    AgentAffectationDTO createAffectation(CreateAffectationCommand command);
}
