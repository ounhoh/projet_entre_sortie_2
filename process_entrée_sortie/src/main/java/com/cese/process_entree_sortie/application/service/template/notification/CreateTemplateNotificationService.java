/* 

package com.cese.process_entree_sortie.application.service.template.notification;

import com.cese.process_entree_sortie.application.dto.template.entree.notification.CreateTemplateNotificationCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateNotificationDTO;
import com.cese.process_entree_sortie.application.port.in.template.notification.CreateTemplateNotificationApi;
import com.cese.process_entree_sortie.application.port.out.notification.TemplateNotificationSpi;
import com.cese.process_entree_sortie.domain.Notification.model.TemplateNotification;
import com.cese.process_entree_sortie.domain.utils.ValueObject.NiveauNotification;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TacheType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CreateTemplateNotificationService implements CreateTemplateNotificationApi {
    
    private final TemplateNotificationSpi templateNotificationSpi;
    private final TemplateNotificationConverter converter;
    
    public CreateTemplateNotificationService(TemplateNotificationSpi templateNotificationSpi, TemplateNotificationConverter converter) {
        this.templateNotificationSpi = templateNotificationSpi;
        this.converter = converter;
    }
    
    @Override
    public TemplateNotificationDTO createNotification(CreateTemplateNotificationCommand command) {
        TemplateNotification nouvelleNotification = new TemplateNotification(
            UUID.randomUUID(),
            NiveauNotification.valueOf(command.niveauGenerique()),
            command.codeNotification(),
            command.objet(),
            command.description(),
            TacheType.valueOf(command.typeEnvoyeur()),
            command.templateTacheId(),
            command.templateGroupeTacheId()
        );
        
        TemplateNotification notificationSauvegarde = templateNotificationSpi.save(nouvelleNotification);
        return converter.convertirEnDTO(notificationSauvegarde);
    }
}
*/