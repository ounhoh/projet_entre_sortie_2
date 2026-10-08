package com.cese.process_entree_sortie.application.port.out.notification;

import com.cese.process_entree_sortie.application.dto.notification.NotificationDTO;
import com.cese.process_entree_sortie.domain.Notification.model.InstanceNotification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de sortie pour gérer les notifications.
 * L'implémentation peut utiliser email, SMS, système de notification interne, etc.
 */
public interface NotificationSpi {
    /**
     * Envoie une notification aux destinataires spécifiés.
     * 
     * @param notification La notification à envoyer
     */
    void envoyerNotification(NotificationDTO notification);
    
    /**
     * Sauvegarde une notification.
     * 
     * @param notification La notification à sauvegarder
     * @return La notification sauvegardée
     */
    InstanceNotification save(InstanceNotification notification);
    
    /**
     * Trouve une notification par son ID.
     * 
     * @param id L'ID de la notification
     * @return La notification trouvée, ou Optional.empty() si non trouvée
     */
    Optional<InstanceNotification> findById(UUID id);
    
    /**
     * Supprime une notification.
     * 
     * @param notification La notification à supprimer
     */
    void delete(InstanceNotification notification);
    
    /**
     * Trouve toutes les notifications d'un agent.
     * 
     * @param agentId L'ID de l'agent
     * @return La liste des notifications de l'agent
     */
    List<InstanceNotification> findByAgentId(UUID agentId);

    /**
     * Trouve les notifications liées à des groupes ou tâches.
     *
     * @param groupeTacheIds Les IDs de groupes de tâches
     * @param tacheIds Les IDs de tâches
     * @return Les notifications correspondantes
     */
    List<InstanceNotification> findByGroupeTacheIdsOrTacheIds(List<UUID> groupeTacheIds, List<UUID> tacheIds);
    
    /**
     * Compte le nombre de notifications non lues d'un agent.
     * 
     * @param agentId L'ID de l'agent
     * @return Le nombre de notifications non lues
     */
    int countUnreadNotificationByAgentId(UUID agentId);
}
