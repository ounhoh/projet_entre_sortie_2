package com.cese.process_entree_sortie.application.dto.template.sortie;

import java.util.UUID;
import java.util.Map;

public record TemplateTacheDTO(UUID id, String libelle, String code,
                               String description, Map<String, Object> contenu) {
}
