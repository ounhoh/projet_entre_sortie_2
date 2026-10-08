package com.cese.process_entree_sortie.domain.Agent.model;


import com.cese.process_entree_sortie.domain.utils.error.DateInvalideException;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record AgentDirection (UUID agentDirectionID, String codeAgentDirection, String codeAgent, String codeDirection,
                              UUID directionId, LocalDate dateArrivee, LocalDate dateDepart,
                              UUID agentPersonnelId,String numeroBureau) {
    public AgentDirection
    {
        Objects.requireNonNull(directionId);
        Objects.requireNonNull(dateArrivee);
        if (dateArrivee.isAfter(LocalDate.now().plusYears(1)))
        {
            throw new DateInvalideException("Date d'arrivé plus tard qu'1 ans");
        }
    }

    public Optional<UUID> getOPtionalAgentId()
    {
        return Optional.ofNullable(agentPersonnelId);
    }

    public Optional<LocalDate> getOPtionalDateDepart()
    {
        return Optional.ofNullable(dateDepart);
    }

    public AgentDirection associerDateDepart(LocalDate nouvelleDateDepart)
    {
        veriferDateDepart(dateArrivee,nouvelleDateDepart);
        return new AgentDirection(agentDirectionID,codeAgentDirection,codeAgent,codeDirection,
                directionId, dateArrivee,nouvelleDateDepart, agentPersonnelId,numeroBureau);
    }

    private static void veriferDateDepart(LocalDate dateArrivee,LocalDate dateDepart)
    {
        if (dateDepart.isAfter(dateArrivee) || LocalDate.now().isAfter(dateDepart))
        {
            throw  new DateInvalideException("Mauvaise date de départ");
        }
    }
}
