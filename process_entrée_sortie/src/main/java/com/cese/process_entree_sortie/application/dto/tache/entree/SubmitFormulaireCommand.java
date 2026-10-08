package com.cese.process_entree_sortie.application.dto.tache.entree;

import java.util.Map;
import java.util.UUID;

public record SubmitFormulaireCommand(UUID tacheId, Map<String, Object> contenujson) {
}
