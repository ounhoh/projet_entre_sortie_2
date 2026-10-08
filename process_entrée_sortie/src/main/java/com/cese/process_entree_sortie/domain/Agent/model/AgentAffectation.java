package com.cese.process_entree_sortie.domain.Agent.model;

import com.cese.process_entree_sortie.domain.utils.error.AgentAcceuilInvalideException;
import com.cese.process_entree_sortie.domain.utils.error.AgentResponsableInvalideException;
import com.cese.process_entree_sortie.domain.utils.error.ChangementDirectionInvalideException;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record AgentAffectation(String Fonction, UUID agentResponsableId, UUID agentAcceuilId, UUID directionId,
                               UUID agentId) {
    public AgentAffectation {
        Objects.requireNonNull(Fonction);
    }

    public Optional<UUID> getOptionalAgentResponsableId()
    {
        return Optional.ofNullable(agentResponsableId);
    }

    public Optional<UUID> getOptionalAgentAcceuilId()
    {
        return Optional.ofNullable(agentAcceuilId);
    }

    public AgentAffectation changerFonction(String newFonction)
    {
        return new AgentAffectation(newFonction,agentResponsableId,
                agentAcceuilId,directionId,agentId);
    }
    public AgentAffectation changerAgentResponsable(UUID newAgentResponsableId)
    {
        if (agentId.equals(newAgentResponsableId))
        {
            throw  new AgentResponsableInvalideException("Agent d'acceuil ne peux pas être notre agent");
        }
        return new AgentAffectation(Fonction,newAgentResponsableId,agentAcceuilId,directionId,agentId);
    }

    public AgentAffectation changerAgentAcceuil(UUID newAgentAcceuilId)
    {
        if (agentId.equals(newAgentAcceuilId))
        {
            throw  new AgentAcceuilInvalideException("Agent responsable ne peut pas être notre agent");
        }
        return new AgentAffectation(Fonction,agentResponsableId,newAgentAcceuilId,directionId,agentId);
    }

    public AgentAffectation changerDirectionEtAffectation(UUID newDirectionId, String newFonction)
    {
        if (newDirectionId.equals(directionId))
        {
            throw new ChangementDirectionInvalideException("impossible de changer de direction pour aller dans la même" +
                    "direction");
        }
        return new AgentAffectation(newFonction,agentResponsableId,
                agentAcceuilId,newDirectionId,agentId);
    }
}
