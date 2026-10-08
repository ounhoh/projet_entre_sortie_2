package com.cese.process_entree_sortie.application.service.notification;

import com.cese.process_entree_sortie.application.port.in.notification.MarkAllNotificationReadApi;
import com.cese.process_entree_sortie.application.port.out.notification.NotificationSpi;
import com.cese.process_entree_sortie.domain.Notification.model.InstanceNotification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MarkAllNotificationReadService implements MarkAllNotificationReadApi {
    
    private final NotificationSpi notificationSpi;
    
    public MarkAllNotificationReadService(NotificationSpi notificationSpi) {
        this.notificationSpi = notificationSpi;
    }
    
    @Override
    public void markAllNotificationRead(UUID agentID) {
        List<InstanceNotification> notifications = notificationSpi.findByAgentId(agentID);
        
        for (InstanceNotification notification : notifications) {
            InstanceNotification notifModifiee = notification.marquertouteNofifCommeLu();
            notificationSpi.save(notifModifiee);
        }
    }
}
