package com.cese.process_entree_sortie.application.dto.direction.entree;

import java.util.UUID;

public record UpdateDirectionCommand (UUID id, String code,String libelle) {
}
