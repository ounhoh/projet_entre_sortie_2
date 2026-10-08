/* 

package com.cese.process_entree_sortie.application.service.template.notification;

import com.cese.process_entree_sortie.application.dto.template.entree.notification.UpdateTemplateNotificationCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateNotificationDTO;
import com.cese.process_entree_sortie.application.port.in.template.notification.UpdateTemplateNotificationApi;
import com.cese.process_entree_sortie.application.port.out.notification.TemplateNotificationSpi;
import com.cese.process_entree_sortie.domain.Notification.model.TemplateNotification;
import com.cese.process_entree_sortie.domain.utils.ValueObject.NiveauNotification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateTemplateNotificationService implements UpdateTemplateNotificationApi {
    
    private final TemplateNotificationSpi templateNotificationSpi;
    private final TemplateNotificationConverter converter;
    
    public UpdateTemplateNotificationService(TemplateNotificationSpi templateNotificationSpi, TemplateNotificationConverter converter) {
        this.templateNotificationSpi = templateNotificationSpi;
        this.converter = converter;
    }
    
    @Override
    public TemplateNotificationDTO updateTemplateNotification(UpdateTemplateNotificationCommand command) {
        TemplateNotification notification = templateNotificationSpi.findById(command.templateNotificationId())
            .orElseThrow(() -> new RuntimeException("Template notification non trouvée avec l'ID: " + command.templateNotificationId()));
        
        TemplateNotification notificationModifiee = new TemplateNotification(
            notification.id(),
            command.niveauGenerique() != null ? NiveauNotification.valueOf(command.niveauGenerique()) : notification.niveau(),
            notification.codeNotification(),
            command.objet() != null ? command.objet() : notification.object(),
            command.description() != null ? command.description() : notification.description(),
            notification.typeEnvoyeur(),
            notification.tacheId(),
            notification.groupeTacheId()
        );
        
        TemplateNotification notificationSauvegarde = templateNotificationSpi.save(notificationModifiee);
        return converter.convertirEnDTO(notificationSauvegarde);
    }
}
*/