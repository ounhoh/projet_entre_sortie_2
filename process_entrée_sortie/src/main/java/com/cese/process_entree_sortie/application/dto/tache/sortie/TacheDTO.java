package com.cese.process_entree_sortie.application.dto.tache.sortie;

import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TacheType;

import java.time.LocalDate;
import java.util.UUID;
import java.util.Map;

public record TacheDTO(UUID id, String code, String libelle, StatutTache statut, String description,
                       Map<String,Object> contenu, Map<String, Object> reponses, LocalDate dateEcheance, TacheType type) {
    
    /**
     * Constructeur de compatibilité pour maintenir la compatibilité avec le code existant
     */
    public TacheDTO(UUID id, String code, String libelle, StatutTache statut, String description,
                    Map<String,Object> contenu, LocalDate dateEcheance, TacheType type) {
        this(id, code, libelle, statut, description, contenu, null, dateEcheance, type);
    }
}