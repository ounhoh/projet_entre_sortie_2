package com.cese.process_entree_sortie.domain.Tache.model;

import com.cese.process_entree_sortie.domain.utils.ValueObject.TacheType;

import java.util.Objects;
import java.util.UUID;

public record TemplateTache (UUID id, String code, String libelle,
                             String description,TacheType type, int delaijour,
                             TacheContenu contenu, UUID dependanceId){
    public TemplateTache {
        Objects.requireNonNull(id);
        Objects.requireNonNull(code);
        Objects.requireNonNull(libelle);
        Objects.requireNonNull(description);
        // dependanceId peut être null (tâche sans dépendance)
    }

}
