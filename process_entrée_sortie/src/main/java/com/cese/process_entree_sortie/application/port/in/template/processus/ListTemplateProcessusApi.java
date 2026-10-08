package com.cese.process_entree_sortie.application.port.in.template.processus;

import com.cese.process_entree_sortie.application.dto.template.entree.processus.ListTemplateQuery;
import com.cese.process_entree_sortie.application.dto.template.sortie.TemplateProcessusDTO;

import java.util.List;
public interface ListTemplateProcessusApi {
    List<TemplateProcessusDTO>  getListTemplateProcessus(ListTemplateQuery query);
}
