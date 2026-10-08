package com.cese.process_entree_sortie.application.dto.tache.sortie;

import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TacheType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * DTO pour un groupe de tâches dans l'arbre des dépendances
 */
public record GroupeTacheNoeudDTO(
    UUID id,
    String code,
    String libelle,
    StatutTache statut,
    LocalDate dateEcheance,
    Integer ordre,
    List<TacheNoeudDTO> tachesInternes, // Tâches directement dans le groupe (sans dépendances)
    Optional<NoeudTacheDTO> filsGauche,
    Optional<NoeudTacheDTO> filsDroit,
    List<NoeudTacheDTO> enfants // Dépendances (autres groupes ou tâches)
) implements NoeudTacheDTO {
    
    @Override
    public TacheType type() {
        return TacheType.groupeTache;
    }
}
