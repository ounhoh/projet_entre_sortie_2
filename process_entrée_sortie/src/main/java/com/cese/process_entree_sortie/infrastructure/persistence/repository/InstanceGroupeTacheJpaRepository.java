package com.cese.process_entree_sortie.infrastructure.persistence.repository;

import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.InstanceGroupeTacheEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InstanceGroupeTacheJpaRepository extends JpaRepository<InstanceGroupeTacheEntity, UUID> {
    List<InstanceGroupeTacheEntity> findByProcessusId(UUID processusId);
    
    @Query("SELECT DISTINCT igt FROM InstanceGroupeTacheEntity igt JOIN igt.agentAssigneIdList a WHERE a = :agentId")
    List<InstanceGroupeTacheEntity> findByAgentId(@Param("agentId") UUID agentId);
    
    @Query("SELECT igt FROM InstanceGroupeTacheEntity igt WHERE igt.statut = :statut AND :agentId MEMBER OF igt.agentAssigneIdList")
    List<InstanceGroupeTacheEntity> findByStatutAndAgent(@Param("statut") StatutTache statut, @Param("agentId") UUID agentId);
    
    List<InstanceGroupeTacheEntity> findByStatut(StatutTache statut);
    
    @Query("SELECT igt FROM InstanceGroupeTacheEntity igt WHERE igt.statut = :statut AND :agentId MEMBER OF igt.agentAssigneIdList AND igt.processusId IN (SELECT ip.id FROM InstanceProcessusEntity ip WHERE ip.directionConcerneeId = :directionId)")
    List<InstanceGroupeTacheEntity> findByStatutAndAgentAndDirection(@Param("statut") StatutTache statut, @Param("agentId") UUID agentId, @Param("directionId") UUID directionId);
    
    @Query("SELECT igt FROM InstanceGroupeTacheEntity igt JOIN igt.agentAssigneIdList a WHERE a = :agentId AND igt.dateEcheance < CURRENT_DATE AND igt.statut != 'fait'")
    List<InstanceGroupeTacheEntity> findEnRetardByAgent(@Param("agentId") UUID agentId);
}