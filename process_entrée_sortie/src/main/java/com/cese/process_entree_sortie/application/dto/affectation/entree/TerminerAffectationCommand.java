package com.cese.process_entree_sortie.application.dto.affectation.entree;

import java.time.LocalDate;
import java.util.UUID;

public record TerminerAffectationCommand(UUID affectationId, LocalDate localDate) {
}
