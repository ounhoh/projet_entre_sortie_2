package com.cese.process_entree_sortie.application.port.in.dependance;

import com.cese.process_entree_sortie.application.dto.tache.sortie.DependanceDTO;

import java.util.UUID;
import java.util.List;

public interface GetDependanceByProcessusApi {
    List<DependanceDTO> getDependanceByProcessus(UUID processusId);
}
