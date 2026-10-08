package com.cese.process_entree_sortie.application.port.in.tache;

import com.cese.process_entree_sortie.application.dto.tache.entree.DesassignerAgentToTacheCommand;
import com.cese.process_entree_sortie.application.dto.tache.sortie.TacheDTO;

public interface DesassignerAgentToTacheApi {
    TacheDTO desassignerAgenToTache(DesassignerAgentToTacheCommand command);
}
