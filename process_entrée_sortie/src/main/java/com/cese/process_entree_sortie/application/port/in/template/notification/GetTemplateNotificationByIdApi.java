package com.cese.process_entree_sortie.application.port.in.template.notification;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateNotificationDTO;

import java.util.UUID;

public interface GetTemplateNotificationByIdApi {
    TemplateNotificationDTO getTemplateNotificationById(UUID templateNotificationId);
}
