package com.cese.process_entree_sortie.application.port.in.template.dependance;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateDependanceDTO;

import java.util.List;
import java.util.UUID;

public interface GetDependanceByTemplateProcessusApi {
    List<TemplateDependanceDTO> getDependanceByTemplateProcessus(UUID templateProcessusId);
}
