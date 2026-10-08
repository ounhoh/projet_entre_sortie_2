package com.cese.process_entree_sortie.application.port.in.dependance;

import com.cese.process_entree_sortie.application.dto.tache.sortie.DependanceDTO;

import java.util.UUID;

public interface GetDependanceTacheApi {
    DependanceDTO getDependanceTache(UUID tacheId);
}
