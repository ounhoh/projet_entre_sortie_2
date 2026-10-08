package com.cese.process_entree_sortie.domain.Tache.model;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Dépendance en structure arbre : 1 tâche source → N tâches cibles
 */
public record InstanceDependance(
    UUID id,
    UUID sourceTacheId,
    List<UUID> cibleTacheIds,
    UUID templateId
) {
    public InstanceDependance {
        Objects.requireNonNull(id);
        Objects.requireNonNull(sourceTacheId);
        Objects.requireNonNull(cibleTacheIds);
        Objects.requireNonNull(templateId);
        
        // Vérifier qu'une tâche ne dépend pas d'elle-même
        if (cibleTacheIds.contains(sourceTacheId)) {
            throw new IllegalArgumentException("Une tâche ne peut pas dépendre d'elle-même");
        }
        
        // Vérifier que la liste des cibles n'est pas vide
        if (cibleTacheIds.isEmpty()) {
            throw new IllegalArgumentException("Une dépendance doit avoir au moins une tâche cible");
        }
    }

    public List<UUID> getCibleTacheIds() {
        return cibleTacheIds;
    }
}
