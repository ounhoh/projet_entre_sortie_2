package com.cese.process_entree_sortie.domain.Tache.model;

import com.cese.process_entree_sortie.domain.utils.error.DependanceInvalideException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Dépendance template en structure arbre : 1 tâche source → N tâches cibles
 */
public record TemplateDependance(
    UUID id,
    UUID sourceTacheId,
    List<UUID> cibleTacheIds
) {
    public TemplateDependance {
        Objects.requireNonNull(id);
        Objects.requireNonNull(sourceTacheId);
        Objects.requireNonNull(cibleTacheIds);
        
        // Vérifier qu'une tâche ne dépend pas d'elle-même
        if (cibleTacheIds.contains(sourceTacheId)) {
            throw new DependanceInvalideException("Une tâche ne peut pas dépendre d'elle-même");
        }
        
        // Vérifier que la liste des cibles n'est pas vide
        if (cibleTacheIds.isEmpty()) {
            throw new DependanceInvalideException("Une dépendance doit avoir au moins une tâche cible");
        }
    }
}
