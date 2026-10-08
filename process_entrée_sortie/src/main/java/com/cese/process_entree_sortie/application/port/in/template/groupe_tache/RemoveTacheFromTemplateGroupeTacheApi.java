package com.cese.process_entree_sortie.application.port.in.template.groupe_tache;

import com.cese.process_entree_sortie.application.dto.template.entree.groupe_tache.RemoveTacheFromTemplateGroupeTacheCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateGroupeTacheDTO;

//Retire une tache du groupe
public interface RemoveTacheFromTemplateGroupeTacheApi {
    TemplateGroupeTacheDTO removeTacheFromGroupeTache(RemoveTacheFromTemplateGroupeTacheCommand command);
}
