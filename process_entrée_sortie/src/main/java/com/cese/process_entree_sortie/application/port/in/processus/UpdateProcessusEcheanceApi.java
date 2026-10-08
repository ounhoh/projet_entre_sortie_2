package com.cese.process_entree_sortie.application.port.in.processus;

import com.cese.process_entree_sortie.application.dto.processus.entree.UpdateEcheanceCommand;
import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessusDTO;

public interface UpdateProcessusEcheanceApi {
   ProcessusDTO updateProcessusEcheance(UpdateEcheanceCommand command);
}
