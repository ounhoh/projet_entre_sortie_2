package com.cese.process_entree_sortie.application.service.notification;

import com.cese.process_entree_sortie.application.port.in.notification.CountUnreadNotificationApi;
import com.cese.process_entree_sortie.application.port.out.notification.NotificationSpi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CountUnreadNotificationService implements CountUnreadNotificationApi {
    
    private final NotificationSpi notificationSpi;
    
    public CountUnreadNotificationService(NotificationSpi notificationSpi) {
        this.notificationSpi = notificationSpi;
    }
    
    @Override
    public int countUnreadNotification(UUID agentId) {
        return notificationSpi.countUnreadNotificationByAgentId(agentId);
    }
}
