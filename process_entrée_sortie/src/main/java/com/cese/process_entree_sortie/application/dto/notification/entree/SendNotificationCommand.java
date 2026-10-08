package com.cese.process_entree_sortie.application.dto.notification.entree;

import com.cese.process_entree_sortie.application.dto.notification.NotificationDTO;
import java.util.UUID;
import java.util.List;

public record SendNotificationCommand(
    NotificationDTO.TypeNotification type,
    String titre,
    String message,
    UUID processusId,
    UUID groupeTacheId, // Optionnel
    UUID tacheId, // Optionnel
    UUID directionId, // Optionnel
    List<UUID> destinaire
) {
}
