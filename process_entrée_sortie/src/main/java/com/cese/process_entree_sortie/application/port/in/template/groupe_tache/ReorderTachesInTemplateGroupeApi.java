package com.cese.process_entree_sortie.application.port.in.template.groupe_tache;

import com.cese.process_entree_sortie.application.dto.template.entree.groupe_tache.ReorderTacheInGroupeCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateGroupeTacheDTO;

// réordonner les tâches dans le groupe
public interface ReorderTachesInTemplateGroupeApi {
    TemplateGroupeTacheDTO reoderTacheInTemplateGroupe(ReorderTacheInGroupeCommand command);
}
