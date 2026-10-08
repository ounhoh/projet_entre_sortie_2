package com.cese.process_entree_sortie.infrastructure.persistence.repository;

import com.cese.process_entree_sortie.domain.utils.ValueObject.StatutTache;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.InstanceTacheEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface InstanceTacheJpaRepository extends JpaRepository<InstanceTacheEntity, UUID> {
    @Query("SELECT it FROM InstanceTacheEntity it WHERE it.groupeTacheId IN (SELECT igt.id FROM InstanceGroupeTacheEntity igt WHERE igt.processusId = :processusId)")
    List<InstanceTacheEntity> findByProcessusId(@Param("processusId") UUID processusId);
    
    List<InstanceTacheEntity> findByStatut(StatutTache statut);
    
    @Query("SELECT it FROM InstanceTacheEntity it WHERE it.dateEcheance < :today AND it.statut != 'fait'")
    List<InstanceTacheEntity> findEnRetard(@Param("today") LocalDate today);
    
    List<InstanceTacheEntity> findByGroupeTacheId(UUID groupeId);
    
    @Query("SELECT it FROM InstanceTacheEntity it WHERE it.groupeTacheId IN :groupeIds")
    List<InstanceTacheEntity> findByGroupeTacheIdIn(@Param("groupeIds") List<UUID> groupeIds);
}