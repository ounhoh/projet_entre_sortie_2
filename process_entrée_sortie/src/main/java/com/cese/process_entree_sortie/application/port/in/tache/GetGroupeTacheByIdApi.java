package com.cese.process_entree_sortie.application.port.in.tache;

import com.cese.process_entree_sortie.application.dto.tache.sortie.GroupeTacheDTO;

import java.util.UUID;

public interface GetGroupeTacheByIdApi {
    GroupeTacheDTO getGroupeTacheById(UUID groupeTacheId);
}
