package com.cese.process_entree_sortie.application.port.in.tache;

import com.cese.process_entree_sortie.application.dto.tache.sortie.TacheDTO;

import java.util.UUID;

// démarrer la tache
public interface StartTacheApi {
    TacheDTO startTache(UUID tacheId);
}
