package com.cese.process_entree_sortie.application.port.in.template.groupe_tache;

import com.cese.process_entree_sortie.application.dto.template.entree.groupe_tache.UpdateTemplateGroupeTacheCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateGroupeTacheDTO;

public interface UpdateTemplateGroupeTacheApi {
    TemplateGroupeTacheDTO updateGroupeTache(UpdateTemplateGroupeTacheCommand command);
}
