package com.cese.process_entree_sortie.application.port.in.template.processus;

import com.cese.process_entree_sortie.domain.Processus.model.TemplateProcessus;

import java.util.UUID;

// désactive le processus (le rendant inutilisable)
public interface DesactivateTemplateProcessusApi {
    TemplateProcessus desactivateProcessus(UUID templateId);
}
