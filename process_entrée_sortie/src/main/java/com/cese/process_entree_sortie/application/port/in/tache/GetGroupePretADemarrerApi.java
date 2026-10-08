package com.cese.process_entree_sortie.application.port.in.tache;

import com.cese.process_entree_sortie.application.dto.tache.sortie.GroupeTacheDTO;

import java.util.List;
import java.util.UUID;

public interface GetGroupePretADemarrerApi {
    List<GroupeTacheDTO> getGroupePretADemarrer(UUID processusId);
}
