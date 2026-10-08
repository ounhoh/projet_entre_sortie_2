package com.cese.process_entree_sortie.infrastructure.persistence.repository;

import com.cese.process_entree_sortie.infrastructure.persistence.entity.StatutProcessusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StatutProcessusJpaRepository extends JpaRepository<StatutProcessusEntity, UUID> {
    java.util.Optional<StatutProcessusEntity> findByCodeStatut(String codeStatut);
    
    @Query("SELECT s FROM StatutProcessusEntity s WHERE s.templateProcessusId = :templateProcessusId ORDER BY s.ordre ASC")
    List<StatutProcessusEntity> findByTemplateProcessusId(@Param("templateProcessusId") UUID templateProcessusId);
}