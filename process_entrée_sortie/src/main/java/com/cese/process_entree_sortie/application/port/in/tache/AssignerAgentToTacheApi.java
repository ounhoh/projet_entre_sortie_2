package com.cese.process_entree_sortie.application.port.in.tache;

import com.cese.process_entree_sortie.application.dto.tache.entree.AssignerAgentToTacheCommand;
import com.cese.process_entree_sortie.application.dto.tache.sortie.TacheDTO;

public interface AssignerAgentToTacheApi {
    TacheDTO assignerAgentToTache(AssignerAgentToTacheCommand command);
}
