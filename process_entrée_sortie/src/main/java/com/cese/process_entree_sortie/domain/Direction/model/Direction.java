package com.cese.process_entree_sortie.domain.Direction.model;

import java.util.Objects;
import java.util.UUID;

public record Direction (UUID id, String codeDirection, String libDirection) {
    public Direction {
       Objects.requireNonNull(id);
       Objects.requireNonNull(codeDirection);
       Objects.requireNonNull(libDirection);
    }
}
