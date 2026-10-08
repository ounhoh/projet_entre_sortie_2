package com.cese.process_entree_sortie.application.dto.tache.sortie;

import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TacheType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface commune pour représenter un nœud dans l'arbre des dépendances.
 * Peut être soit une tâche, soit un groupe de tâches.
 */
public sealed interface NoeudTacheDTO permits TacheNoeudDTO, GroupeTacheNoeudDTO {
    UUID id();
    String code();
    String libelle();
    StatutTache statut();
    TacheType type();
    LocalDate dateEcheance();
    Integer ordre(); // Ordre d'affichage dans le parent (peut être null pour les racines)
    Optional<NoeudTacheDTO> filsGauche(); // Tâche enfant (si type = tache)
    Optional<NoeudTacheDTO> filsDroit(); // Groupe enfant (si type = groupeTache)
    List<NoeudTacheDTO> enfants(); // Tous les enfants (dépendances) dans l'ordre
}
