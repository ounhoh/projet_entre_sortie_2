package com.cese.process_entree_sortie.application.port.in.template.processus;

import com.cese.process_entree_sortie.application.dto.template.entree.processus.CreateTemplateProcessusCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateProcessusDTO;

public interface CreateTemplateProcessusApi {
    TemplateProcessusDTO createTemplateProcessus(CreateTemplateProcessusCommand command);
}
