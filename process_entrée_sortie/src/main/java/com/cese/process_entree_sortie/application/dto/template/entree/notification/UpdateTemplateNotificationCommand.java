package com.cese.process_entree_sortie.application.dto.template.entree.notification;

import java.util.UUID;

public record UpdateTemplateNotificationCommand(UUID templateNotificationId, String objet, String description,
                                                String niveauGenerique) {
}
