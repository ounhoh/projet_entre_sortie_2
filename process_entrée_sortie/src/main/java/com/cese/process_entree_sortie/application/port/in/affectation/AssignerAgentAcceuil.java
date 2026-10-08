package com.cese.process_entree_sortie.application.port.in.affectation;

import com.cese.process_entree_sortie.application.dto.affectation.entree.AssignerAgentAcceuilCommand;
import com.cese.process_entree_sortie.application.dto.affectation.sortie.AgentAffectationDTO;

public interface AssignerAgentAcceuil {
    AgentAffectationDTO assignerAgentAcceuil(AssignerAgentAcceuilCommand command);
}
