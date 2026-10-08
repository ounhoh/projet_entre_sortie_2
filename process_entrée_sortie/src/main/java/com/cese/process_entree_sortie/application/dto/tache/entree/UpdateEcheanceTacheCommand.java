package com.cese.process_entree_sortie.application.dto.tache.entree;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateEcheanceTacheCommand(UUID tacheId, LocalDate nouvelleEcheance) {
    
}
