package com.cese.process_entree_sortie.application.dto.template.sortie;


import java.util.UUID;

public record TemplateNotificationDTO(UUID id, String codeNotification,String object, String description,
                                     String niveauNotification, String Direction) {
}
