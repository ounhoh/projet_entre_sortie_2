package com.cese.process_entree_sortie.domain.Processus.model;

import java.util.Objects;
import java.util.UUID;

public record StatutProcessus(UUID id, String codeStatut, String libStatut) {

    public StatutProcessus {
        Objects.requireNonNull(id);
        Objects.requireNonNull(codeStatut);
        Objects.requireNonNull(libStatut);
    }
}
