package com.cese.process_entree_sortie.application.dto.agent.entree;

import java.util.UUID;

public record SearchAgentsCommand(String termeDeRecherche, String role, UUID directionId) {
}
