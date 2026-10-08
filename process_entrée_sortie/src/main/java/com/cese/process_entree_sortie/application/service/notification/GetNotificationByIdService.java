package com.cese.process_entree_sortie.application.service.notification;

import com.cese.process_entree_sortie.application.dto.notification.sortie.NotificationDTO;
import com.cese.process_entree_sortie.application.port.in.notification.GetNotificationByIdApi;
import com.cese.process_entree_sortie.application.port.out.notification.NotificationSpi;
import com.cese.process_entree_sortie.domain.Notification.model.InstanceNotification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetNotificationByIdService implements GetNotificationByIdApi {
    
    private final NotificationSpi notificationSpi;
    private final NotificationConverter converter;
    
    public GetNotificationByIdService(NotificationSpi notificationSpi, NotificationConverter converter) {
        this.notificationSpi = notificationSpi;
        this.converter = converter;
    }
    
    @Override
    public NotificationDTO getNotificationById(UUID notificationId) {
        InstanceNotification notification = notificationSpi.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification non trouvée avec l'ID: " + notificationId));
        return converter.convertirEnDTO(notification);
    }
}
