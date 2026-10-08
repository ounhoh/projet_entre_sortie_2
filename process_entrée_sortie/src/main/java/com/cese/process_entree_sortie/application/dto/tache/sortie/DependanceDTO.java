package com.cese.process_entree_sortie.application.dto.tache.sortie;

import java.util.List;
import java.util.UUID;

/**
 * DTO pour les dépendances d'instance.
 * Nouvelle structure : 1 tâche source → N tâches cibles (uniquement entre tâches).
 */
public record DependanceDTO(UUID id, 
                          UUID sourceTacheId,
                          List<UUID> cibleTacheIds) {
}
