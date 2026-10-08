package com.cese.process_entree_sortie.application.port.out.processus;

import com.cese.process_entree_sortie.domain.Processus.model.TemplateProcessus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TemplateProcessusSpi {
    TemplateProcessus save(TemplateProcessus templateProcessus);
    Optional<TemplateProcessus> findById(UUID templateId);
    List<TemplateProcessus> findAll();
    List<TemplateProcessus> findActifs();
    List<TemplateProcessus> findByType(String type);
    void delete(TemplateProcessus templateProcessus);
    long count();
}
