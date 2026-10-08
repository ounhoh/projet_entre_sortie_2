package com.cese.process_entree_sortie.application.port.in.tache;

import com.cese.process_entree_sortie.application.dto.tache.entree.UpdateEcheanceTacheCommand;
import com.cese.process_entree_sortie.application.dto.tache.sortie.TacheDTO;

public interface UpdateDateEchanceApi {
    TacheDTO updateDateEcheance(UpdateEcheanceTacheCommand command);
}
