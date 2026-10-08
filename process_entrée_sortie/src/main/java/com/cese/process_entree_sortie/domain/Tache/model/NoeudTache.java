package com.cese.process_entree_sortie.domain.Tache.model;

import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TacheType;

import java.util.UUID;
import java.util.Optional;

public sealed interface NoeudTache permits  InstanceGroupeTache, InstanceTache
{
    UUID id();
    StatutTache statut();
    String libelle();
    String code();
    TacheType type();
    Optional<InstanceTache> getTache();
    Optional<InstanceGroupeTache> getGroupeTache();
}
