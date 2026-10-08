package com.cese.process_entree_sortie.application.dto.notification;

import java.util.List;
import java.util.UUID;

/**
 * DTO pour les notifications sans template.
 * Trois types de notifications :
 * - INFO_DIRECTION_TERMINEE : Quand une direction a fini tous ses groupes de tâches
 * - INFO_GROUPE_DISPONIBLE : Quand un groupe de tâches devient disponible (dépendances satisfaites)
 * - ALERTE_DELAI : Quand une tâche/groupe approche ou dépasse son délai
 */
public record NotificationDTO(
    TypeNotification type,
    String titre,
    String message,
    UUID processusId,
    UUID groupeTacheId, // Optionnel, pour les notifications de groupe
    UUID tacheId, // Optionnel, pour les notifications de tâche
    UUID directionId, // Optionnel, pour les notifications de direction
    List<UUID> destinataires // Liste des IDs des agents destinataires
) {
    public enum TypeNotification {
        INFO_DIRECTION_TERMINEE,
        INFO_GROUPE_DISPONIBLE,
        ALERTE_DELAI
    }
}
