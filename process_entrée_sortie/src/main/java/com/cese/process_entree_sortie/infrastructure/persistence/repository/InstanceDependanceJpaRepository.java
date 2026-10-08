package com.cese.process_entree_sortie.infrastructure.persistence.repository;

import com.cese.process_entree_sortie.infrastructure.persistence.entity.InstanceDependanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstanceDependanceJpaRepository extends JpaRepository<InstanceDependanceEntity, UUID> {
    @Query("SELECT DISTINCT d FROM InstanceDependanceEntity d " +
           "JOIN InstanceTacheEntity t ON t.id = d.sourceTacheId " +
           "JOIN InstanceGroupeTacheEntity g ON g.id = t.groupeTacheId " +
           "WHERE g.processusId = :processusId")
    List<InstanceDependanceEntity> findByProcessusId(@Param("processusId") UUID processusId);
    
    Optional<InstanceDependanceEntity> findBySourceTacheId(UUID sourceTacheId);
    
    @Query("SELECT DISTINCT id FROM InstanceDependanceEntity id " +
           "JOIN id.cibles c " +
           "WHERE c.cibleTacheId = :tacheId")
    List<InstanceDependanceEntity> findByTacheId(@Param("tacheId") UUID tacheId);
}