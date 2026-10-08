package com.cese.process_entree_sortie.application.port.in.template.groupe_tache.visualisation;

import com.cese.process_entree_sortie.application.dto.visualisation.sortie.AvancementDTO;

import java.util.UUID;

public interface AvancementApi {
    AvancementDTO getAvancementProcessus(UUID processusId);
    AvancementDTO getAvancementGroupe(UUID groupeId);
}
