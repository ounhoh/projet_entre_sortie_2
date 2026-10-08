package com.cese.process_entree_sortie.application.dto.notification.sortie;

import com.cese.process_entree_sortie.domain.utils.ValueObject.NiveauNotification;

import java.time.LocalDate;
import java.util.UUID;
import java.util.List;
public record NotificationDTO(UUID id, NiveauNotification niveauNotification, String object, LocalDate dateEnvoie,
                              String directionEnvoyeur, List<UUID> agentAyantRecuNotificaiton, UUID processusId) {
}
