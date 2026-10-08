package com.cese.process_entree_sortie.application.port.in.processus;

import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessusDTO;
import com.cese.process_entree_sortie.domain.Processus.model.InstanceProcessus;

import java.util.UUID;

public interface GetProcessusByIdApi {
    ProcessusDTO getProcessById(UUID processusId);
}
