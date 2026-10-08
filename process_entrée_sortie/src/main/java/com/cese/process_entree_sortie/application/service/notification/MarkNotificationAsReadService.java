package com.cese.process_entree_sortie.application.service.notification;

import com.cese.process_entree_sortie.application.port.in.notification.MarkNotificationAsReadApi;
import com.cese.process_entree_sortie.application.port.out.notification.NotificationSpi;
import com.cese.process_entree_sortie.domain.Notification.model.InstanceNotification;
import com.cese.process_entree_sortie.domain.utils.ValueObject.ModeLecture;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class MarkNotificationAsReadService implements MarkNotificationAsReadApi {
    
    private final NotificationSpi notificationSpi;
    
    public MarkNotificationAsReadService(NotificationSpi notificationSpi) {
        this.notificationSpi = notificationSpi;
    }
    
    @Override
    public void markNotificationAsREed(UUID notification) {
        InstanceNotification notif = notificationSpi.findById(notification)
            .orElseThrow(() -> new RuntimeException("Notification non trouvée avec l'ID: " + notification));
        
        // TODO: L'API n'a pas d'agentId - à déterminer comment obtenir l'agent courant
        // Pour l'instant, on marque toutes les notifications comme lues
        InstanceNotification notifModifiee = notif.marquertouteNofifCommeLu();
        notificationSpi.save(notifModifiee);
    }
}
