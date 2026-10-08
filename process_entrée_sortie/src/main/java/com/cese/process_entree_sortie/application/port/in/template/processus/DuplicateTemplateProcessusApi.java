package com.cese.process_entree_sortie.application.port.in.template.processus;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateProcessusDTO;

import java.util.UUID;

public interface DuplicateTemplateProcessusApi {
    TemplateProcessusDTO duplicateTemplateProcesus(UUID templateId);
}
