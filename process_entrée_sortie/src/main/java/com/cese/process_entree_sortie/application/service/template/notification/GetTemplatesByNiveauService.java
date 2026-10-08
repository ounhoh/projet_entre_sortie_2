/* 

package com.cese.process_entree_sortie.application.service.template.notification;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateNotificationDTO;
import com.cese.process_entree_sortie.application.port.in.template.notification.GetTemplatesByNiveauApi;
import com.cese.process_entree_sortie.application.port.out.notification.TemplateNotificationSpi;
import com.cese.process_entree_sortie.domain.Notification.model.TemplateNotification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetTemplatesByNiveauService implements GetTemplatesByNiveauApi {
    
    private final TemplateNotificationSpi templateNotificationSpi;
    private final TemplateNotificationConverter converter;
    
    public GetTemplatesByNiveauService(TemplateNotificationSpi templateNotificationSpi, TemplateNotificationConverter converter) {
        this.templateNotificationSpi = templateNotificationSpi;
        this.converter = converter;
    }
    
    @Override
    public List<TemplateNotificationDTO> getTemplatesByNiveau(String niveau) {
        List<TemplateNotification> notifications = templateNotificationSpi.findByNiveau(niveau);
        return notifications.stream()
            .map(converter::convertirEnDTO)
            .collect(Collectors.toList());
    }
}

*/