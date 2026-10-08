package com.cese.process_entree_sortie.application.dto.template.entree.groupe_tache;

import java.util.UUID;

public record UpdateTemplateGroupeTacheCommand(UUID groupeTacheId, String libelle,
                                               int ordre, UUID directionId) {
}
