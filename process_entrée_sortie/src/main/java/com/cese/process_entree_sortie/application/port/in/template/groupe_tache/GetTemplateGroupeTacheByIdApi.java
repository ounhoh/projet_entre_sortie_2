package com.cese.process_entree_sortie.application.port.in.template.groupe_tache;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateGroupeTacheDTO;

import java.util.UUID;

public interface GetTemplateGroupeTacheByIdApi {
    TemplateGroupeTacheDTO getTemplateGroupeTacheById(UUID groupeId);
}
