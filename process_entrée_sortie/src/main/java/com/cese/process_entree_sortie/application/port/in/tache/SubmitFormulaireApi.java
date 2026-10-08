package com.cese.process_entree_sortie.application.port.in.tache;

import com.cese.process_entree_sortie.application.dto.tache.entree.SubmitFormulaireCommand;
import com.cese.process_entree_sortie.application.dto.tache.sortie.TacheDTO;

public interface SubmitFormulaireApi {
    TacheDTO submitFormulaire(SubmitFormulaireCommand command);
}
