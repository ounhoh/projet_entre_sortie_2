package com.cese.process_entree_sortie.application.port.out.tache;

import com.cese.process_entree_sortie.domain.Tache.model.InstanceGroupeTache;
import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupeTacheSpi {
    InstanceGroupeTache save(InstanceGroupeTache groupeTache);
    Optional<InstanceGroupeTache> findById(UUID groupeTacheId);
    List<InstanceGroupeTache> findByProcessusId(UUID processusId);
    List<InstanceGroupeTache> findByAgentId(UUID agentId);
    List<InstanceGroupeTache> findByStatutAndAgent(StatutTache statut,UUID agentId);
    List<InstanceGroupeTache> findByStatut(StatutTache statut);
    List<InstanceGroupeTache> findByStatutAndAgentAndDirection(StatutTache statut, UUID agentId, UUID directionId);
    List<InstanceGroupeTache> findEnRetardByAgent(UUID agentId);
    void delete(InstanceGroupeTache groupeTache);
}
