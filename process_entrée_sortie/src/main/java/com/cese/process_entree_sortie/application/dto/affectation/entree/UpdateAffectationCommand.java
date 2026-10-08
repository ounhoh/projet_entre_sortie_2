package com.cese.process_entree_sortie.application.dto.affectation.entree;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateAffectationCommand(UUID agentId, UUID directionId, String fonction, String service, LocalDate
                                       dateArrivee,LocalDate DateDepart) {
}
