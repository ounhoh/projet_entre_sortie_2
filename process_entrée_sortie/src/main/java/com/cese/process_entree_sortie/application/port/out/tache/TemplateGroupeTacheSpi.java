package com.cese.process_entree_sortie.application.port.out.tache;

import com.cese.process_entree_sortie.domain.Tache.model.TemplateGroupeTache;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TemplateGroupeTacheSpi {
    TemplateGroupeTache save(TemplateGroupeTache groupeTache);
    Optional<TemplateGroupeTache> findById(UUID groupeId);
    List<TemplateGroupeTache> findByTemplateProcessusId(UUID processusId);
    List<TemplateGroupeTache> findByDirectionId(UUID directionId);
    List<TemplateGroupeTache> findAll();
    void delete(TemplateGroupeTache templateGroupeTache);
    boolean existById(UUID groupeId);
    //compter les groupes d'un template
    long countByTemplateProcessusId(UUID processusId);
}
