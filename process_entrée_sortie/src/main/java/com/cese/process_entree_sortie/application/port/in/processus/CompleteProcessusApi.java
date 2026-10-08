package com.cese.process_entree_sortie.application.port.in.processus;

import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessusDTO;

import java.util.UUID;

public interface CompleteProcessusApi {
    ProcessusDTO completeProcessus(UUID processusId);
}
