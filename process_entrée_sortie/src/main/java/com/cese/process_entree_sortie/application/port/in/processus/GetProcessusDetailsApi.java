package com.cese.process_entree_sortie.application.port.in.processus;

import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessDetailDTO;

import java.util.UUID;

public interface GetProcessusDetailsApi {
    ProcessDetailDTO getProcessusDetail(UUID processId);
}
