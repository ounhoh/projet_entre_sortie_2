package com.cese.process_entree_sortie.application.port.out.dependance;

import com.cese.process_entree_sortie.domain.Tache.model.InstanceDependance;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DependanceSpi {
    InstanceDependance save(InstanceDependance dependance);
    Optional<InstanceDependance> findById(UUID dependanceId);
    List<InstanceDependance> findByProcessusId(UUID processusId);
    // Trouver la dépendance dont une tâche est la source
    Optional<InstanceDependance> findBySourceTacheId(UUID sourceTacheId);
    // Trouver toutes les dépendances où une tâche est une cible (via dependanceId dans InstanceTache)
    List<InstanceDependance> findByTacheId(UUID tacheId);
    void delete(InstanceDependance dependance);
}