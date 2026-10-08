package com.cese.process_entree_sortie.application.dto.affectation.entree;

import java.time.LocalDate;
import java.util.UUID;

public record CreateAffectationCommand(UUID agentId, UUID direction, String fonction, String service,
                                       LocalDate dateArrivee,LocalDate dateDepart) {
}
