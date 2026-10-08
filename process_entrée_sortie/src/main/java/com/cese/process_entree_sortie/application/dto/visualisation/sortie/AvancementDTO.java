package com.cese.process_entree_sortie.application.dto.visualisation.sortie;

import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessDetailDTO;
import com.cese.process_entree_sortie.application.dto.tache.sortie.GroupeTacheDTO;
import com.cese.process_entree_sortie.application.dto.tache.sortie.TacheDTO;

import java.util.List;

public record AvancementDTO(ProcessDetailDTO processus, List<GroupeTacheDTO> groupeTacheList, List<TacheDTO> tachesList) {
}
