package com.cese.process_entree_sortie.application.port.in.affectation;

import com.cese.process_entree_sortie.application.dto.affectation.entree.UpdateAffectationCommand;
import com.cese.process_entree_sortie.application.dto.affectation.sortie.AgentAffectationDTO;

public interface UpdateAffectationApi {
    AgentAffectationDTO updateAffectation(UpdateAffectationCommand command);
}

