package com.cese.process_entree_sortie.application.dto.template.entree.groupe_tache;

import java.util.UUID;

public record RemoveTacheFromTemplateGroupeTacheCommand(UUID groupeId, UUID tacheId) {

}
