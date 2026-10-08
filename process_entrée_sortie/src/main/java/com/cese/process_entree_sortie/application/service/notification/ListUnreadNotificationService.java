package com.cese.process_entree_sortie.application.service.notification;

import com.cese.process_entree_sortie.application.dto.notification.sortie.NotificationDTO;
import com.cese.process_entree_sortie.application.port.in.notification.ListUnreadNotificationApi;
import com.cese.process_entree_sortie.application.port.out.notification.NotificationSpi;
import com.cese.process_entree_sortie.domain.Notification.model.InstanceNotification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ListUnreadNotificationService implements ListUnreadNotificationApi {
    
    private final NotificationSpi notificationSpi;
    private final NotificationConverter converter;
    
    public ListUnreadNotificationService(NotificationSpi notificationSpi, NotificationConverter converter) {
        this.notificationSpi = notificationSpi;
        this.converter = converter;
    }
    
    @Override
    public List<NotificationDTO> getListUnreadNotificationApi(UUID agentId) {
        List<InstanceNotification> notifications = notificationSpi.findByAgentId(agentId);
        // Filtrer les notifications non lues
        return notifications.stream()
            .filter(notif -> notif.reçus().stream()
                .anyMatch(recu -> recu.destinataire().equals(agentId) && !recu.statutLecture()))
            .map(converter::convertirEnDTO)
            .collect(Collectors.toList());
    }
}
