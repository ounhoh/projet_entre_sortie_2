package com.cese.process_entree_sortie.application.dto.agent.entree;

import java.util.UUID;

public record UpdateAgentCommandCommand(UUID agentId, String nom, String prenom, String email, String role) {
}
