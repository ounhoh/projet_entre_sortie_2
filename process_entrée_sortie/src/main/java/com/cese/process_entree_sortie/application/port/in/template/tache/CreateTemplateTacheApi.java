package com.cese.process_entree_sortie.application.port.in.template.tache;

import com.cese.process_entree_sortie.application.dto.template.entree.tache.CreateTemplateTacheCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateTacheDTO;

public interface CreateTemplateTacheApi {
    TemplateTacheDTO createTemplateTache(CreateTemplateTacheCommand command);
}
