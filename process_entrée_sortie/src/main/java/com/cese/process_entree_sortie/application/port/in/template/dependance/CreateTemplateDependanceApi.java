package com.cese.process_entree_sortie.application.port.in.template.dependance;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateDependanceDTO;
import com.cese.process_entree_sortie.application.dto.template.entree.dependance.CreateTemplateDependanceCommand;

public interface CreateTemplateDependanceApi {
    TemplateDependanceDTO createTemplateDependance(CreateTemplateDependanceCommand command);
}
