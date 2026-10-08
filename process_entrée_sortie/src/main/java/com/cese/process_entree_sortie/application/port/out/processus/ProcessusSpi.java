package com.cese.process_entree_sortie.application.port.out.processus;

import com.cese.process_entree_sortie.domain.Processus.model.InstanceProcessus;
import com.cese.process_entree_sortie.domain.Processus.model.StatutProcessus;
import com.cese.process_entree_sortie.domain.utils.ValueObject.TypeProcessus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcessusSpi {
    InstanceProcessus save(InstanceProcessus processus);
    Optional<InstanceProcessus> findById(UUID id);
    List<InstanceProcessus> findByAgentId(UUID agentId);
    List<InstanceProcessus> findByStatut(StatutProcessus statutProcessus);
    boolean existByAgentIdAndStatut(UUID agentId, StatutProcessus statutProcessus);
    List<InstanceProcessus> findActifs();
    List<InstanceProcessus> findEnRetards();
    List<InstanceProcessus> findByDirectionId(UUID directionId);
    void delete(InstanceProcessus instanceProcessus);
    long count();
    long countByStatut(StatutProcessus statutProcessus);
    long countByType(TypeProcessus typeProcessus);

}
