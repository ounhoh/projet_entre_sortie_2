package com.cese.process_entree_sortie.application.dto.processus.sortie;

import com.cese.process_entree_sortie.application.dto.tache.sortie.NoeudTacheDTO;
import java.util.List;

/**
 * DTO représentant l'arbre complet des dépendances d'un processus.
 * Contient les nœuds racines (sans dépendances entrantes) et les données brutes pour debug.
 */
public record ProcessusArbreDTO(
    ProcessusDTO processInfo,
    List<NoeudTacheDTO> racines // Nœuds racines (sans dépendances entrantes), triés par ordre
) {
}
