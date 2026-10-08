package com.cese.process_entree_sortie.application.port.in.notification;

import com.cese.process_entree_sortie.application.dto.notification.sortie.NotificationDTO;

import java.util.List;
import java.util.UUID;

public interface ListNotificationsByAgentApi {
    List<NotificationDTO> getListNotificationByAgent(UUID agentId);
}
