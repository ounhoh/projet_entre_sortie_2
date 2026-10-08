package com.cese.process_entree_sortie.application.port.in.processus;

import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessusArbreDTO;
import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessusArbreFiltreDTO;

import java.util.UUID;

public interface GetProcessusArbreApi {
    ProcessusArbreDTO getProcessusArbre(UUID processusId);
    
    ProcessusArbreFiltreDTO getProcessusArbreFiltre(
            UUID processusId,
            ProcessusArbreFiltreDTO.StatutFiltre statutFiltre,
            UUID agentFiltre);
}
