package com.cese.process_entree_sortie.application.service.notification;

import com.cese.process_entree_sortie.application.dto.notification.entree.SendReminderCommand;
import com.cese.process_entree_sortie.application.dto.notification.sortie.NotificationDTO;
import com.cese.process_entree_sortie.application.port.in.notification.SendReminderApi;
import com.cese.process_entree_sortie.application.port.out.notification.NotificationSpi;
import com.cese.process_entree_sortie.domain.Notification.model.InstanceNotification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SendReminderService implements SendReminderApi {
    
    private final NotificationSpi notificationSpi;
    private final NotificationConverter converter;
    
    public SendReminderService(NotificationSpi notificationSpi, NotificationConverter converter) {
        this.notificationSpi = notificationSpi;
        this.converter = converter;
    }
    
    @Override
    public NotificationDTO sendReminder(SendReminderCommand command) {
        InstanceNotification notificationExistante = notificationSpi.findById(command.notificationId())
            .orElseThrow(() -> new RuntimeException("Notification non trouvée avec l'ID: " + command.notificationId()));
        
        InstanceNotification notificationRappel = notificationExistante.envoyerRappel(command.dateReminder());
        InstanceNotification notificationSauvegarde = notificationSpi.save(notificationRappel);
        return converter.convertirEnDTO(notificationSauvegarde);
    }
}