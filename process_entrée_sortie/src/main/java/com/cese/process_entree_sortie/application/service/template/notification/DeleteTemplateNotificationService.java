/* 

package com.cese.process_entree_sortie.application.service.template.notification;

import com.cese.process_entree_sortie.application.port.in.template.notification.DeleteTemplateNotificationApi;
import com.cese.process_entree_sortie.application.port.out.notification.TemplateNotificationSpi;
import com.cese.process_entree_sortie.domain.Notification.model.TemplateNotification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeleteTemplateNotificationService implements DeleteTemplateNotificationApi {
    
    private final TemplateNotificationSpi templateNotificationSpi;
    
    public DeleteTemplateNotificationService(TemplateNotificationSpi templateNotificationSpi) {
        this.templateNotificationSpi = templateNotificationSpi;
    }
    
    @Override
    public void deleteTemplateNotificationbyId(UUID templateNotificationId) {
        TemplateNotification notification = templateNotificationSpi.findById(templateNotificationId)
            .orElseThrow(() -> new RuntimeException("Template notification non trouvée avec l'ID: " + templateNotificationId));
        templateNotificationSpi.delete(notification);
    }
}
*/