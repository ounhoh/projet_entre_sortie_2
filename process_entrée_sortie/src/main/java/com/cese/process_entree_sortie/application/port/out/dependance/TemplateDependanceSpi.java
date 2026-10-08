package com.cese.process_entree_sortie.application.port.out.dependance;

import com.cese.process_entree_sortie.domain.Tache.model.TemplateDependance;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
public interface TemplateDependanceSpi {
    TemplateDependance save(TemplateDependance dependance);
    Optional<TemplateDependance> findById(UUID dependanceId);
    List<TemplateDependance> findByTemplateProcessusId(UUID processusId);
    void delete(TemplateDependance dependance);
}
