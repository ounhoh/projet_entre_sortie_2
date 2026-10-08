package com.cese.process_entree_sortie.application.port.in.template.tache;

import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateTacheDTO;

import java.util.List;
import java.util.UUID;

public interface ListTachesByGroupeTacheApi {
    List<TemplateTacheDTO> getListTacheByGroupeTache(UUID groupeTacheID);
}
