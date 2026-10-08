package com.cese.process_entree_sortie.application.port.out.tache;

import com.cese.process_entree_sortie.domain.Tache.model.InstanceTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TacheSpi {
    InstanceTache save(InstanceTache instanceTache);
    Optional<InstanceTache> findById(UUID tacheId);
    List<InstanceTache> findbyProcessusId(UUID processusId);
    List<InstanceTache> findByAgentId(UUID agentId);
    List<InstanceTache> findByStatut(StatutTache statutTache);
    List<InstanceTache> findEnRetard();
    List<InstanceTache> findByGroupeId(UUID groupeId);
    void delete(InstanceTache instanceTache);
}
