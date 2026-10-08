package com.cese.process_entree_sortie.application.port.in.template.processus;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateProcessusDTO;

import java.util.List;

public interface GetTemplatesProcesssusByTypeApi {
    List<TemplateProcessusDTO> getTemplateProcessusByType(String type);
}
