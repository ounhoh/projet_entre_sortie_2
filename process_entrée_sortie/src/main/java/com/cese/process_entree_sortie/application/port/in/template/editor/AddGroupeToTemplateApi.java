package com.cese.process_entree_sortie.application.port.in.template.editor;

import com.cese.process_entree_sortie.application.dto.template.entree.processus.AddGroupeToTemplateCommand;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateProcessusDTO;

public interface AddGroupeToTemplateApi {
     TemplateProcessusDTO addGroupeToTemplate(AddGroupeToTemplateCommand command);
}
