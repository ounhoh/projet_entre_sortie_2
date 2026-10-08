package com.cese.process_entree_sortie.application.dto.template.entree.tache;

import com.cese.process_entree_sortie.domain.utils.ValueObject.TacheType;
import java.util.UUID;
public record CreateTemplateTacheCommand(String code, String libelle, String description,
                                         TacheType type, int delaijour, UUID dependanceId) {
}
