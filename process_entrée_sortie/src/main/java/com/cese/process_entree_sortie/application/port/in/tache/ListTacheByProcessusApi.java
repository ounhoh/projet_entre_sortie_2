package com.cese.process_entree_sortie.application.port.in.tache;

import com.cese.process_entree_sortie.application.dto.tache.sortie.TacheDTO;

import java.util.List;
import java.util.UUID;

public interface ListTacheByProcessusApi {
    List<TacheDTO> getListTacheByProcessus(UUID processusId);
}
