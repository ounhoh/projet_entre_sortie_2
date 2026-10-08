package com.cese.process_entree_sortie.application.port.in.processus;

import com.cese.process_entree_sortie.application.dto.processus.sortie.ProcessusDTO;
import com.cese.process_entree_sortie.application.dto.processus.sortie.StatutProcessusDTO;

import java.util.List;

public interface ListProcessusByStatutApi {
    List<ProcessusDTO> getListProcessusByStatut(StatutProcessusDTO statutProcessus);
}
