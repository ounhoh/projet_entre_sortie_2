package com.cese.process_entree_sortie.application.port.in.template.processus;

import com.cese.process_entree_sortie.application.dto.template.entree.processus.UpdateTemplateProcessusCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateProcessusDTO;

public interface UpdateTemplateProcessusApi {
    TemplateProcessusDTO updateTemplateProcessus(UpdateTemplateProcessusCommand command);
}
