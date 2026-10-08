package com.cese.process_entree_sortie.application.port.in.notification;

import com.cese.process_entree_sortie.application.dto.notification.entree.SendReminderCommand;
import com.cese.process_entree_sortie.application.dto.notification.sortie.NotificationDTO;

import java.util.UUID;

public interface SendReminderApi {
    NotificationDTO sendReminder(SendReminderCommand command);
}
