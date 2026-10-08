package com.cese.process_entree_sortie.application.service.notification;

import com.cese.process_entree_sortie.application.dto.notification.NotificationDTO;
import com.cese.process_entree_sortie.application.dto.notification.entree.SendNotificationCommand;
import com.cese.process_entree_sortie.application.port.in.notification.SendNotificationApi;
import com.cese.process_entree_sortie.application.port.out.notification.NotificationSpi;
import com.cese.process_entree_sortie.domain.Notification.model.InstanceNotification;
import com.cese.process_entree_sortie.domain.Notification.model.NotificationAgentDirection;
import com.cese.process_entree_sortie.domain.utils.ValueObject.ModeLecture;
import com.cese.process_entree_sortie.domain.utils.ValueObject.NiveauNotification;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TacheType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class SendNotificationService implements SendNotificationApi {
    
    private final NotificationSpi notificationSpi;
    private final NotificationConverter converter;
    
    public SendNotificationService(NotificationSpi notificationSpi, NotificationConverter converter) {
        this.notificationSpi = notificationSpi;
        this.converter = converter;
    }
    
    @Override
    public com.cese.process_entree_sortie.application.dto.notification.sortie.NotificationDTO sendNotification(SendNotificationCommand command) {
        // Déterminer le type d'envoyeur selon les infos disponibles
        TacheType typeEnvoyeur = determineTypeEnvoyeur(command);
        
        // Déterminer le niveau de notification selon le type
        NiveauNotification niveau = determineNiveau(command.type());
        
        // Créer le NotificationDTO pour l'envoi (nouveau système)
        com.cese.process_entree_sortie.application.dto.notification.NotificationDTO notificationDTO = new com.cese.process_entree_sortie.application.dto.notification.NotificationDTO(
            command.type(),
            command.titre(),
            command.message(),
            command.processusId(),
            command.groupeTacheId(),
            command.tacheId(),
            command.directionId(),
            command.destinaire()
        );
        
        // Envoyer la notification (log)
        notificationSpi.envoyerNotification(notificationDTO);
        
        // Créer les destinataires pour InstanceNotification
        List<NotificationAgentDirection> destinataires = command.destinaire().stream()
            .map(agentId -> new NotificationAgentDirection(UUID.randomUUID(), agentId, ModeLecture.manel))
            .collect(Collectors.toList());
        
        // Créer l'instance de notification (sans template)
        InstanceNotification notification = new InstanceNotification(
            UUID.randomUUID(),
            niveau,
            command.titre(), // Utiliser le titre comme codeNotification
            typeEnvoyeur,
            null, // templateNotification = null (nouveau système sans template)
            command.groupeTacheId(),
            command.tacheId(),
            destinataires
        );
        
        // Sauvegarder la notification
        InstanceNotification notificationSauvegarde = notificationSpi.save(notification);
        
        // Convertir en DTO de sortie pour l'API
        return converter.convertirEnDTO(notificationSauvegarde);
    }
    
    private TacheType determineTypeEnvoyeur(SendNotificationCommand command) {
        if (command.tacheId() != null) {
            return TacheType.tache;
        } else if (command.groupeTacheId() != null) {
            return TacheType.groupeTache;
        } else {
            // Par défaut, considérer comme une notification de processus
            return TacheType.groupeTache; // ou créer un nouveau type si nécessaire
        }
    }
    
    private NiveauNotification determineNiveau(NotificationDTO.TypeNotification type) {
        return switch (type) {
            case ALERTE_DELAI -> NiveauNotification.alerte;
            case INFO_DIRECTION_TERMINEE, INFO_GROUPE_DISPONIBLE -> NiveauNotification.information;
        };
    }
}