package com.cese.process_entree_sortie.application.port.out.tache;

import com.cese.process_entree_sortie.domain.Tache.model.TemplateTache;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TemplateTacheSpi {
    TemplateTache save(TemplateTache tache);
    Optional<TemplateTache> findById(UUID tacheId);
    List<TemplateTache> findByTemplateGroupeId(UUID groupeId);
    List<TemplateTache> findAll();
    void delete(TemplateTache templateTache);
    boolean existById(UUID id);
    // compte tache dans un gropue
    long countByTemplateGroupeId(UUID templateGroupeId);
}
