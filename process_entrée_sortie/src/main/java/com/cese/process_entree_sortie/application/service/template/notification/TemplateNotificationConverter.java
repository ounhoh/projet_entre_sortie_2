/* 

package com.cese.process_entree_sortie.application.service.template.notification;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateNotificationDTO;
import com.cese.process_entree_sortie.domain.Notification.model.TemplateNotification;
import org.springframework.stereotype.Component;

@Component
public class TemplateNotificationConverter {
    
    public TemplateNotificationDTO convertirEnDTO(TemplateNotification notification) {
        return new TemplateNotificationDTO(
            notification.id(),
            notification.codeNotification(),
            notification.object(),
            notification.description(),
            notification.niveau().toString(),
            null // Direction - à récupérer si nécessaire
        );
    }
}
*/