package com.cese.process_entree_sortie.domain.Notification.model;

import com.cese.process_entree_sortie.domain.utils.ValueObject.ModeLecture;
import com.cese.process_entree_sortie.domain.utils.ValueObject.NiveauNotification;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TacheType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public record InstanceNotification(UUID id, NiveauNotification niveau, LocalDateTime dateEnvoie, String codeNotification,
                                   TacheType typeEnvoyeur, TemplateNotification templateNotification,
                                   UUID groupeTacheId, UUID tacheId, List<NotificationAgentDirection> reçus) {

    public InstanceNotification {
        // templateNotification est maintenant optionnel (peut être null si on n'utilise pas de template)
        // Objects.requireNonNull(templateNotification); // Commenté car on n'utilise plus de templates
        Objects.requireNonNull(typeEnvoyeur);
        Objects.requireNonNull(codeNotification);
        Objects.requireNonNull(niveau);

        // vérfier qu'il y a bien un envoyeur
        if (typeEnvoyeur.equals(TacheType.tache) && getOptionalTache().isEmpty())
        {
            throw  new IllegalArgumentException("notification censé être tache mais aucune tache liée");
        }
        if (typeEnvoyeur.equals(TacheType.groupeTache) && getOptionalGroupeTache().isEmpty())
        {
            throw  new IllegalArgumentException("notification censé être groupe tache mais aucun groupe tache liée");
        }
    }


    public InstanceNotification(UUID id,NiveauNotification niveau, String codeNotification,
                                TacheType typeEnvoyeur, TemplateNotification templateNotification,
                                UUID groupeTache, UUID tache, List<NotificationAgentDirection> destinataires)
    {
        this(id,niveau,LocalDateTime.now(), codeNotification, typeEnvoyeur, templateNotification, groupeTache, tache,
                destinataires);
    }


    public InstanceNotification marquerNotifLuPourAgent(UUID agent, ModeLecture modeLecture) // marquer pour un agent que la notif a été lu
    {
        List<NotificationAgentDirection> notif = List.copyOf(reçus);
        notif = notif.stream().map(recu -> {
            if (agent.equals(recu.destinataire()))
            {
                return recu.marquerCommeLu(modeLecture);
            }
            return recu;
        }).toList();
        return new InstanceNotification(id,niveau,dateEnvoie,codeNotification,typeEnvoyeur,templateNotification,groupeTacheId
        ,tacheId,notif);
    }

    public InstanceNotification marquertouteNofifCommeLu() // pour tout les agent la notif est mis à lu
    {
        List<NotificationAgentDirection> notif = List.copyOf(reçus);
        notif = notif.stream().map(recu -> {
            if (!recu.statutLecture())
            {
                return recu.marquerCommeLu(ModeLecture.auto);
            }
            return recu;
        }).toList();
        return new InstanceNotification(id,niveau,dateEnvoie,codeNotification,typeEnvoyeur,templateNotification,groupeTacheId
                ,tacheId,notif);
    }

    public Optional<UUID> getOptionalGroupeTache()
    {
        return Optional.ofNullable(groupeTacheId);
    }

    public Optional<UUID> getOptionalTache()
    {
        return Optional.ofNullable(tacheId);
    }

    public InstanceNotification envoyerRappel(LocalDate dateReminder)
    {
        LocalDateTime dateRappel = dateEnvoie.plusDays(1);
        return new InstanceNotification(id,niveau,dateRappel,codeNotification,typeEnvoyeur,templateNotification,groupeTacheId
        ,tacheId,reçus);
    }
}
