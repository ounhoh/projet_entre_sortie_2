package com.cese.process_entree_sortie.application.port.in.affectation;

import com.cese.process_entree_sortie.application.dto.affectation.entree.AssignerResponsableCommand;
import com.cese.process_entree_sortie.application.dto.affectation.sortie.AgentAffectationDTO;

public interface AgentResponsableApi {
    AgentAffectationDTO assignerResponsable(AssignerResponsableCommand command);
}
