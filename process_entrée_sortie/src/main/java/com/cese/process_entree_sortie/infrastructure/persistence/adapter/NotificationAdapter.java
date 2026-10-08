package com.cese.process_entree_sortie.infrastructure.persistence.adapter;

import com.cese.process_entree_sortie.application.dto.notification.NotificationDTO;
import com.cese.process_entree_sortie.application.port.out.notification.NotificationSpi;
import com.cese.process_entree_sortie.domain.Notification.model.InstanceNotification;
import com.cese.process_entree_sortie.domain.Notification.model.NotificationAgentDirection;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.InstanceNotificationEntity;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.NotificationAgentDirectionEntity;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.InstanceNotificationJpaRepository;
import com.cese.process_entree_sortie.infrastructure.persistence.repository.NotificationAgentDirectionJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter pour gérer les notifications.
 * Gère l'envoi de notifications et la persistance des InstanceNotification.
 */
@Repository
@Transactional
public class NotificationAdapter implements NotificationSpi {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationAdapter.class);
    
    private final InstanceNotificationJpaRepository jpaRepository;
    private final NotificationAgentDirectionJpaRepository agentDirectionJpaRepository;
    
    public NotificationAdapter(
            InstanceNotificationJpaRepository jpaRepository,
            NotificationAgentDirectionJpaRepository agentDirectionJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.agentDirectionJpaRepository = agentDirectionJpaRepository;
    }
    
    @Override
    public void envoyerNotification(NotificationDTO notification) {
        // TODO: Implémenter l'envoi réel de notifications (email, SMS, système interne, etc.)
        logger.info("=== NOTIFICATION ===");
        logger.info("Type: {}", notification.type());
        logger.info("Titre: {}", notification.titre());
        logger.info("Message: {}", notification.message());
        logger.info("Processus ID: {}", notification.processusId());
        logger.info("Destinataires: {}", notification.destinataires());
        logger.info("===================");
        
        // Pour l'instant, on log simplement
        // Dans le futur, on pourra :
        // - Envoyer un email aux agents
        // - Créer une entrée dans une table de notifications
        // - Envoyer une notification push
        // - etc.
    }
    
    @Override
    public InstanceNotification save(InstanceNotification notification) {
        InstanceNotificationEntity entity = convertirEnEntity(notification);
        InstanceNotificationEntity entitySauvegardee = jpaRepository.save(entity);
        
        // Sauvegarder les destinataires (NotificationAgentDirection)
        if (notification.reçus() != null) {
            // Supprimer les anciens destinataires
            agentDirectionJpaRepository.findByNotificationId(notification.id())
                .forEach(agentDirectionJpaRepository::delete);
            
            // Sauvegarder les nouveaux destinataires
            for (NotificationAgentDirection recu : notification.reçus()) {
                NotificationAgentDirectionEntity recuEntity = convertirRecuEnEntity(recu, notification.id());
                agentDirectionJpaRepository.save(recuEntity);
            }
        }
        
        return chargerComplet(entitySauvegardee.getId());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<InstanceNotification> findById(UUID id) {
        return jpaRepository.findById(id)
            .map(entity -> chargerComplet(entity.getId()));
    }
    
    @Override
    public void delete(InstanceNotification notification) {
        // Supprimer les destinataires d'abord
        agentDirectionJpaRepository.findByNotificationId(notification.id())
            .forEach(agentDirectionJpaRepository::delete);
        
        // Supprimer la notification
        jpaRepository.deleteById(notification.id());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstanceNotification> findByAgentId(UUID agentId) {
        return jpaRepository.findByAgentId(agentId).stream()
            .map(entity -> chargerComplet(entity.getId()))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstanceNotification> findByGroupeTacheIdsOrTacheIds(List<UUID> groupeTacheIds, List<UUID> tacheIds) {
        if ((groupeTacheIds == null || groupeTacheIds.isEmpty()) &&
            (tacheIds == null || tacheIds.isEmpty())) {
            return List.of();
        }
        
        List<InstanceNotificationEntity> notifications = new java.util.ArrayList<>();
        if (groupeTacheIds != null && !groupeTacheIds.isEmpty()) {
            notifications.addAll(jpaRepository.findByGroupeTacheIdIn(groupeTacheIds));
        }
        if (tacheIds != null && !tacheIds.isEmpty()) {
            notifications.addAll(jpaRepository.findByTacheIdIn(tacheIds));
        }
        
        return notifications.stream()
            .map(entity -> chargerComplet(entity.getId()))
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public int countUnreadNotificationByAgentId(UUID agentId) {
        return jpaRepository.countUnreadNotificationByAgentId(agentId);
    }
    
    private InstanceNotificationEntity convertirEnEntity(InstanceNotification notification) {
        InstanceNotificationEntity entity = new InstanceNotificationEntity();
        entity.setId(notification.id());
        entity.setNiveau(notification.niveau());
        entity.setDateEnvoie(notification.dateEnvoie());
        entity.setCodeNotification(notification.codeNotification());
        entity.setTypeEnvoyeur(notification.typeEnvoyeur());
        
        // Note: templateNotificationId n'est plus utilisé si on a commenté les templates
        // Pour l'instant, on peut mettre null ou un UUID par défaut
        // TODO: Adapter selon votre nouveau modèle sans template
        entity.setTemplateNotificationId(UUID.randomUUID()); // Valeur temporaire
        
        entity.setGroupeTacheId(notification.groupeTacheId());
        entity.setTacheId(notification.tacheId());
        
        return entity;
    }
    
    private NotificationAgentDirectionEntity convertirRecuEnEntity(NotificationAgentDirection recu, UUID notificationId) {
        NotificationAgentDirectionEntity entity = new NotificationAgentDirectionEntity();
        entity.setNotificationId(notificationId);
        entity.setAgentDirectionId(recu.destinataire());
        entity.setStatutLecture(recu.statutLecture());
        entity.setModeLecture(recu.modeLecture());
        return entity;
    }
    
    private InstanceNotification chargerComplet(UUID notificationId) {
        InstanceNotificationEntity entity = jpaRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification non trouvée avec l'ID: " + notificationId));
        
        // Charger les destinataires
        List<NotificationAgentDirectionEntity> recusEntities = agentDirectionJpaRepository.findByNotificationId(notificationId);
        List<NotificationAgentDirection> recus = recusEntities.stream()
            .map(this::convertirRecuEnDomain)
            .collect(Collectors.toList());
        
        // Créer InstanceNotification sans template (templateNotification = null)
        // Le template n'est plus utilisé dans le nouveau système
        return new InstanceNotification(
            entity.getId(),
            entity.getNiveau(),
            entity.getDateEnvoie(),
            entity.getCodeNotification(),
            entity.getTypeEnvoyeur(),
            null, // templateNotification = null car on n'utilise plus de templates
            entity.getGroupeTacheId(),
            entity.getTacheId(),
            recus
        );
    }
    
    private NotificationAgentDirection convertirRecuEnDomain(NotificationAgentDirectionEntity entity) {
        // NotificationAgentDirection a besoin d'un id (UUID) et d'un destinataire (UUID)
        // L'id est l'agentDirectionId, et le destinataire est aussi l'agentDirectionId
        UUID agentDirectionId = entity.getAgentDirectionId();
        return new NotificationAgentDirection(
            agentDirectionId, // id
            agentDirectionId, // destinataire (même valeur)
            entity.getStatutLecture(),
            entity.getModeLecture()
        );
    }
}
