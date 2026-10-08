package com.cese.process_entree_sortie.infrastructure.persistence.repository;

import com.cese.process_entree_sortie.infrastructure.persistence.entity.TemplateDependanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TemplateDependanceJpaRepository extends JpaRepository<TemplateDependanceEntity, UUID> {
    @Query(value = "SELECT DISTINCT td.* FROM template_dependance td " +
           "WHERE " +
           "  td.source_tache_id IN " +
           "    (SELECT tgat.template_tache_id FROM template_groupe_association_tache tgat " +
           "     JOIN template_groupe_tache tg ON tgat.template_groupe_id = tg.id " +
           "     WHERE tg.template_processus_id = :processusId) " +
           "  OR " +
           "  td.id IN " +
           "    (SELECT tdc.dependance_id FROM template_dependance_cibles tdc " +
           "     JOIN template_groupe_association_tache tgat ON tdc.cible_tache_id = tgat.template_tache_id " +
           "     JOIN template_groupe_tache tg ON tgat.template_groupe_id = tg.id " +
           "     WHERE tg.template_processus_id = :processusId)",
           nativeQuery = true)
    List<TemplateDependanceEntity> findByTemplateProcessusId(@Param("processusId") UUID processusId);
    
    Optional<TemplateDependanceEntity> findBySourceTacheId(UUID sourceTacheId);
    
    @Query(value = "SELECT DISTINCT td.* FROM template_dependance td " +
           "JOIN template_dependance_cibles tdc ON td.id = tdc.dependance_id " +
           "WHERE tdc.cible_tache_id = :tacheId",
           nativeQuery = true)
    List<TemplateDependanceEntity> findByCibleTacheId(@Param("tacheId") UUID tacheId);
}