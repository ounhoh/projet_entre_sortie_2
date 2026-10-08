package com.cese.process_entree_sortie.application.dto.tache.sortie;

import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TacheType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * DTO pour une tâche dans l'arbre des dépendances
 */
public record TacheNoeudDTO(
    UUID id,
    String code,
    String libelle,
    StatutTache statut,
    LocalDate dateEcheance,
    Integer ordre,
    Map<String, Object> contenu, // Contenu spécifique à la tâche
    Optional<NoeudTacheDTO> filsGauche,
    Optional<NoeudTacheDTO> filsDroit,
    List<NoeudTacheDTO> enfants
) implements NoeudTacheDTO {
    
    @Override
    public TacheType type() {
        return TacheType.tache;
    }
}
