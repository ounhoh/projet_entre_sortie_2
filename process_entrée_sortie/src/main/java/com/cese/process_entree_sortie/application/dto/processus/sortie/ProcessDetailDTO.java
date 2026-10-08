package com.cese.process_entree_sortie.application.dto.processus.sortie;

import com.cese.process_entree_sortie.application.dto.tache.sortie.DependanceDTO;
import com.cese.process_entree_sortie.application.dto.tache.sortie.GroupeTacheDTO;
import java.util.List;

public record ProcessDetailDTO(ProcessusDTO processInfo,
                               List<GroupeTacheDTO> groupeTacheList,
                               List<DependanceDTO> dependanceList
                               ) {
}
