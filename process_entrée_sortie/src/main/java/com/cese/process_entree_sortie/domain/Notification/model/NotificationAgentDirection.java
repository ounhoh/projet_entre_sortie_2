package com.cese.process_entree_sortie.domain.Notification.model;

import com.cese.process_entree_sortie.domain.Agent.model.AgentDirection;
import com.cese.process_entree_sortie.domain.utils.ValueObject.ModeLecture;
import com.cese.process_entree_sortie.domain.utils.error.NotificationStatutInvalidException;
import org.springframework.boot.Banner;

import java.util.Objects;
import java.util.UUID;

// destinataire = agentdirection
public record NotificationAgentDirection(UUID id,UUID destinataire, Boolean statutLecture,
                                         ModeLecture modeLecture) {
    public NotificationAgentDirection {
        Objects.requireNonNull(id);
        Objects.requireNonNull(destinataire);
        Objects.requireNonNull(statutLecture);
    }
    public NotificationAgentDirection(UUID id , UUID destinataire, ModeLecture lecture)
    {
       this(id,destinataire,Boolean.FALSE,lecture);
    }

    public NotificationAgentDirection marquerCommeLu(ModeLecture newModeLecture) // marquer la notification comme étant lu
    {
        if (!statutLecture)
        {

            return new NotificationAgentDirection(id,destinataire,Boolean.TRUE,modeLecture);
        }
        else
        {
            throw new NotificationStatutInvalidException("mise à jour ayant déjà été lu");
        }
    }
    public NotificationAgentDirection changerEtatLecture(Boolean statutLecture) // changer l'état de lecture de la notif
    {
       return new NotificationAgentDirection(id,destinataire,statutLecture,modeLecture);
    }
}
