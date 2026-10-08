package com.cese.process_entree_sortie.application.dto.agent.entree;

import java.util.UUID;

public record ChangerEmailCommand(UUID agentId, String newEmail) {
}
