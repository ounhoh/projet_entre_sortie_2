package com.cese.process_entree_sortie.application.dto.template.entree.notification;

import java.util.UUID;

public record CreateTemplateNotificationCommand(String codeNotification, String objet, String description,
                                                String niveauGenerique, String typeEnvoyeur,
                                                UUID templateTacheId,UUID templateGroupeTacheId) {
}
