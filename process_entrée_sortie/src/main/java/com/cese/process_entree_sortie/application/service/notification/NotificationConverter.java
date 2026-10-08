package com.cese.process_entree_sortie.application.service.notification;

import com.cese.process_entree_sortie.application.dto.notification.sortie.NotificationDTO;
import com.cese.process_entree_sortie.application.port.out.tache.GroupeTacheSpi;
import com.cese.process_entree_sortie.application.port.out.tache.TacheSpi;
import com.cese.process_entree_sortie.domain.Notification.model.InstanceNotification;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceGroupeTache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class NotificationConverter {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationConverter.class);
    
    private final GroupeTacheSpi groupeTacheSpi;
    private final TacheSpi tacheSpi;
    
    public NotificationConverter(GroupeTacheSpi groupeTacheSpi, TacheSpi tacheSpi) {
        this.groupeTacheSpi = groupeTacheSpi;
        this.tacheSpi = tacheSpi;
    }
    
    public NotificationDTO convertirEnDTO(InstanceNotification notification) {
        // Extraire les IDs des agents ayant reçu la notification
        List<UUID> agentIds = notification.reçus().stream()
            .map(recu -> recu.destinataire())
            .collect(Collectors.toList());
        
        // Récupérer l'objet depuis le template si disponible, sinon utiliser le codeNotification
        String objet = notification.templateNotification() != null 
            ? notification.templateNotification().object() 
            : notification.codeNotification();
        
        // Récupérer le processusId depuis le groupeTacheId ou tacheId
        UUID processusId = null;
        try {
            if (notification.groupeTacheId() != null) {
                processusId = groupeTacheSpi.findById(notification.groupeTacheId())
                    .map(InstanceGroupeTache::processusId)
                    .orElse(null);
            } else if (notification.tacheId() != null) {
                processusId = tacheSpi.findById(notification.tacheId())
                    .map(tache -> {
                        // La tâche a un groupeTacheId, on doit récupérer le processus depuis le groupe
                        UUID groupeId = tache.groupeTacheId();
                        if (groupeId != null) {
                            return groupeTacheSpi.findById(groupeId)
                                .map(InstanceGroupeTache::processusId)
                                .orElse(null);
                        }
                        return null;
                    })
                    .orElse(null);
            }
        } catch (Exception e) {
            logger.warn("Erreur lors de la récupération du processusId pour la notification {}: {}", 
                       notification.id(), e.getMessage());
        }
        
        return new NotificationDTO(
            notification.id(),
            notification.niveau(),
            objet,
            notification.dateEnvoie().toLocalDate(),
            null, // directionEnvoyeur - à récupérer si nécessaire
            agentIds,
            processusId
        );
    }
}
