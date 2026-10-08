package com.cese.process_entree_sortie.application.dto.processus.sortie;

import com.cese.process_entree_sortie.application.dto.tache.sortie.NoeudTacheDTO;
import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;

import java.util.List;
import java.util.UUID;

/**
 * DTO pour l'arbre filtré avec des métadonnées
 */
public record ProcessusArbreFiltreDTO(
    ProcessusDTO processInfo,
    List<NoeudTacheDTO> racines,
    StatutFiltre statutFiltre,
    UUID agentFiltre, // null si pas de filtre par agent
    int totalNoeuds,
    int noeudsCompletes,
    int noeudsEnCours,
    int noeudsEnAttente,
    double pourcentageAvancement,
    List<NoeudTacheDTO> noeudsPretsADemarrer,
    List<NoeudTacheDTO> noeudsBloques
) {
    public enum StatutFiltre {
        TOUS,
        COMPLETES,
        EN_COURS,
        EN_ATTENTE,
        PRETS_A_DEMARRER,
        BLOQUES
    }
}
