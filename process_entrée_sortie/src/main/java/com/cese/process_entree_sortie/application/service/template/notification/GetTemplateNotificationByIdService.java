/* 

package com.cese.process_entree_sortie.application.service.template.notification;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateNotificationDTO;
import com.cese.process_entree_sortie.application.port.in.template.notification.GetTemplateNotificationByIdApi;
import com.cese.process_entree_sortie.application.port.out.notification.TemplateNotificationSpi;
import com.cese.process_entree_sortie.domain.Notification.model.TemplateNotification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetTemplateNotificationByIdService implements GetTemplateNotificationByIdApi {
    
    private final TemplateNotificationSpi templateNotificationSpi;
    private final TemplateNotificationConverter converter;
    
    public GetTemplateNotificationByIdService(TemplateNotificationSpi templateNotificationSpi, TemplateNotificationConverter converter) {
        this.templateNotificationSpi = templateNotificationSpi;
        this.converter = converter;
    }
    
    @Override
    public TemplateNotificationDTO getTemplateNotificationById(UUID notificationId) {
        TemplateNotification notification = templateNotificationSpi.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Template notification non trouvée avec l'ID: " + notificationId));
        return converter.convertirEnDTO(notification);
    }
}

*/
