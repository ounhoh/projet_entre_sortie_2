package com.cese.process_entree_sortie.application.dto.processus.entree;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public record CreateProcessusCommand(
    UUID agentId, 
    UUID templateProcessusId, 
    LocalDate dateDebut,
    Map<String, Object> formulaireData  // Données du formulaire rempli (optionnel)
) {
}
