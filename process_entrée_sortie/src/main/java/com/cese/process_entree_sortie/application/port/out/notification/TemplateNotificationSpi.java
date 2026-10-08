package com.cese.process_entree_sortie.application.port.out.notification;

import com.cese.process_entree_sortie.domain.Notification.model.TemplateNotification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TemplateNotificationSpi {
    TemplateNotification save(TemplateNotification templateNotification);
    Optional<TemplateNotification> findById(UUID templateId);
    Optional<TemplateNotification> findByCode(String code);
    List<TemplateNotification> findAll();
    List<TemplateNotification> findByNiveau(String niveau);
    void delete(TemplateNotification templateNotification);
}