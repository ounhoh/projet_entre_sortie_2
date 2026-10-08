package com.cese.process_entree_sortie.application.port.in.template.groupe_tache;

import com.cese.process_entree_sortie.application.dto.template.entree.groupe_tache.CreateTemplateGroupeTacheCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateGroupeTacheDTO;

public interface CreateTemplateGroupeTacheApi {
    TemplateGroupeTacheDTO createTemplateGroupeTache(CreateTemplateGroupeTacheCommand command);
}
