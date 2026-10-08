package com.cese.process_entree_sortie.application.port.in.tache;

import com.cese.process_entree_sortie.application.dto.tache.sortie.TacheDTO;
import java.util.List;
import java.util.UUID;

// obtention des taches d'un agent
public interface GetMyTacheApi {
    List<TacheDTO> getMyTache(UUID agentId);
}
