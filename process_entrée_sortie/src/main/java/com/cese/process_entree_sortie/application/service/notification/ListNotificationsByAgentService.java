package com.cese.process_entree_sortie.application.service.notification;

import com.cese.process_entree_sortie.application.dto.notification.sortie.NotificationDTO;
import com.cese.process_entree_sortie.application.port.in.notification.ListNotificationsByAgentApi;
import com.cese.process_entree_sortie.application.port.out.notification.NotificationSpi;
import com.cese.process_entree_sortie.domain.Notification.model.InstanceNotification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ListNotificationsByAgentService implements ListNotificationsByAgentApi {
    
    private final NotificationSpi notificationSpi;
    private final NotificationConverter converter;
    
    public ListNotificationsByAgentService(NotificationSpi notificationSpi, NotificationConverter converter) {
        this.notificationSpi = notificationSpi;
        this.converter = converter;
    }
    
    @Override
    public List<NotificationDTO> getListNotificationByAgent(UUID agentId) {
        List<InstanceNotification> notifications = notificationSpi.findByAgentId(agentId);
        return notifications.stream()
            .map(converter::convertirEnDTO)
            .collect(Collectors.toList());
    }
}
