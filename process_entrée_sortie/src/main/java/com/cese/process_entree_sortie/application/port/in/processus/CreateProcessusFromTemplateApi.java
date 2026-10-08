package com.cese.process_entree_sortie.application.port.in.processus;

import com.cese.process_entree_sortie.application.dto.processus.entree.CreateProcessusCommand;
import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessusDTO;

public interface CreateProcessusFromTemplateApi {
    ProcessusDTO createProcessu(CreateProcessusCommand command);
}
