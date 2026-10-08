package com.cese.process_entree_sortie.application.dto.role.sortie;

import java.util.UUID;

public record RoleDTO(
    UUID id,
    String code,
    String libelle,
    Integer valeur
) {
}
