package com.cese.process_entree_sortie.application.port.in.template.processus;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateProcessusDTO;

import java.util.UUID;

// active le processus pour le rendre visible
public interface ActivateTemplateProcessusApi {
    TemplateProcessusDTO activateTemplateProcessus(UUID templateId);
}
