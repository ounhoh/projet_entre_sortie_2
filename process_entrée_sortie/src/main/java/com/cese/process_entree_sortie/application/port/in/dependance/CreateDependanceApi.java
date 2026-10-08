package com.cese.process_entree_sortie.application.port.in.dependance;

import com.cese.process_entree_sortie.application.dto.tache.entree.CreateDependanceCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateDependanceDTO;

public interface CreateDependanceApi{
    TemplateDependanceDTO createDependance(CreateDependanceCommand command);
}
