 
package com.cese.process_entree_sortie.infrastructure.persistence.adapter;

import com.cese.process_entree_sortie.application.port.out.notification.TemplateNotificationSpi;
import com.cese.process_entree_sortie.domain.Notification.model.TemplateNotification;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.TemplateNotificationEntity;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.TemplateNotificationJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Transactional
public class TemplateNotificationAdapter implements TemplateNotificationSpi {
    
    private final TemplateNotificationJpaRepository jpaRepository;
    
    public TemplateNotificationAdapter(TemplateNotificationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public TemplateNotification save(TemplateNotification templateNotification) {
        TemplateNotificationEntity entity = convertirEnEntity(templateNotification);
        TemplateNotificationEntity entitySauvegardee = jpaRepository.save(entity);
        return convertirEnDomain(entitySauvegardee);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<TemplateNotification> findById(UUID templateId) {
        return jpaRepository.findById(templateId)
            .map(this::convertirEnDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<TemplateNotification> findByCode(String code) {
        return jpaRepository.findByCodeNotification(code)
            .map(this::convertirEnDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TemplateNotification> findAll() {
        return jpaRepository.findAll().stream()
            .map(this::convertirEnDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TemplateNotification> findByNiveau(String niveau) {
        return jpaRepository.findByNiveau(niveau).stream()
            .map(this::convertirEnDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public void delete(TemplateNotification templateNotification) {
        jpaRepository.deleteById(templateNotification.id());
    }
    
    private TemplateNotificationEntity convertirEnEntity(TemplateNotification template) {
        return new TemplateNotificationEntity(
            template.id(),
            template.niveau(),
            template.codeNotification(),
            template.object(),
            template.description(),
            template.typeEnvoyeur(),
            template.tacheId(),
            template.groupeTacheId()
        );
    }
    
    private TemplateNotification convertirEnDomain(TemplateNotificationEntity entity) {
        return new TemplateNotification(
            entity.getId(),
            entity.getNiveau(),
            entity.getCodeNotification(),
            entity.getObject(),
            entity.getDescription(),
            entity.getTypeEnvoyeur(),
            entity.getTacheId(),
            entity.getGroupeTacheId()
        );
    }
}