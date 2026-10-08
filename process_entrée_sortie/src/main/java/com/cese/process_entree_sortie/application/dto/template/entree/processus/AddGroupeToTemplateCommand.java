package com.cese.process_entree_sortie.application.dto.template.entree.processus;

import java.util.UUID;

public record AddGroupeToTemplateCommand(UUID templateProcessusId, UUID groupeId) {
}