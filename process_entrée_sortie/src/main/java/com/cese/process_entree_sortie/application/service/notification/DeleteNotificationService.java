package com.cese.process_entree_sortie.application.service.notification;

import com.cese.process_entree_sortie.application.port.in.notification.DeleteNotificationApi;
import com.cese.process_entree_sortie.application.port.out.notification.NotificationSpi;
import com.cese.process_entree_sortie.domain.Notification.model.InstanceNotification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeleteNotificationService implements DeleteNotificationApi {
    
    private final NotificationSpi notificationSpi;
    
    public DeleteNotificationService(NotificationSpi notificationSpi) {
        this.notificationSpi = notificationSpi;
    }
    
    @Override
    public void deleteNotification(UUID notificationId) {
        InstanceNotification notification = notificationSpi.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification non trouvée avec l'ID: " + notificationId));
        notificationSpi.delete(notification);
    }
}
