package com.cese.process_entree_sortie.domain.Processus.model;

import com.cese.process_entree_sortie.domain.Tache.model.InstanceDependance;
import com.cese.process_entree_sortie.domain.Tache.model.InstanceGroupeTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TypeProcessus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;

public record InstanceProcessus(UUID id, String codeProcessus, LocalDateTime dateCreation,
                                LocalDate dateEcheance, UUID directionConcerneeId, UUID agentId,
                                UUID templateId, UUID statutId,
                                List<InstanceGroupeTache> groupeTachesList,
                                List<InstanceDependance> dependances, TypeProcessus typeProcessus) {
    public InstanceProcessus
    {
        Objects.requireNonNull(id);
        Objects.requireNonNull(codeProcessus);
        Objects.requireNonNull(dateCreation);
        Objects.requireNonNull(dateEcheance);
        Objects.requireNonNull(directionConcerneeId);
        Objects.requireNonNull(agentId);
        Objects.requireNonNull(templateId);
        Objects.requireNonNull(statutId);

    }

    public InstanceProcessus changerStatut(UUID newStatutId)
    {
        return new InstanceProcessus(id, codeProcessus,dateCreation,dateEcheance,
                directionConcerneeId,agentId,templateId,newStatutId,groupeTachesList,dependances,typeProcessus);
    }

    public InstanceProcessus(UUID id, String codeProcessus, LocalDate dateEcheance, UUID directionConcerneeId,
                             UUID agent, UUID templateId,UUID statut,
                             List<InstanceGroupeTache> groupeTachesList,List<InstanceDependance> dependances,
                             TypeProcessus typeProcessus)
    {
        this(id,codeProcessus,LocalDateTime.now(),dateEcheance,directionConcerneeId,
                agent,templateId,statut,groupeTachesList,dependances,typeProcessus);
    }

    public Optional<LocalDate> getOptionalDateEchance()
    {
        return Optional.ofNullable(dateEcheance);
    }


}