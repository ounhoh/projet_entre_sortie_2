package com.cese.process_entree_sortie.application.port.in.notification;


import com.cese.process_entree_sortie.application.dto.notification.entree.SendNotificationCommand;
import com.cese.process_entree_sortie.application.dto.notification.sortie.NotificationDTO;

public interface SendNotificationApi {
    NotificationDTO sendNotification(SendNotificationCommand command);

}
