package com.cese.process_entree_sortie.application.dto.tache.sortie;

import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record GroupeTacheDTO (UUID id, String code, String libelle,
                              StatutTache statut, LocalDate DateEcheance,
                              List<TacheDTO> taches, UUID templateId){
}
