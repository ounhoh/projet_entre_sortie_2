package com.cese.process_entree_sortie.application.port.in.affectation;

import com.cese.process_entree_sortie.application.dto.affectation.entree.TerminerAffectationCommand;
import com.cese.process_entree_sortie.application.dto.affectation.sortie.AgentAffectationDTO;

public interface TerminerAffectationApi {
    AgentAffectationDTO terminerAffectation(TerminerAffectationCommand command);
}
