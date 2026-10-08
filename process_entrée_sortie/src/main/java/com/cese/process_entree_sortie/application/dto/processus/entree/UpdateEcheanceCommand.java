package com.cese.process_entree_sortie.application.dto.processus.entree;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateEcheanceCommand(UUID processId, LocalDate nouvelleEcheance) {
}
