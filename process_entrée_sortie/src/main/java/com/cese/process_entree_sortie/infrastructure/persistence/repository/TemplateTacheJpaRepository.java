package com.cese.process_entree_sortie.infrastructure.persistence.repository;

import com.cese.process_entree_sortie.infrastructure.persistence.entity.TemplateTacheEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TemplateTacheJpaRepository extends JpaRepository<TemplateTacheEntity, UUID> {
    @Query(value = "SELECT tt.* FROM template_tache tt " +
           "WHERE tt.id IN (SELECT tgat.template_tache_id FROM template_groupe_association_tache tgat WHERE tgat.template_groupe_id = :groupeId)",
           nativeQuery = true)
    List<TemplateTacheEntity> findByTemplateGroupeId(@Param("groupeId") UUID groupeId);
    
    @Query(value = "SELECT COUNT(tt.id) FROM template_tache tt " +
           "WHERE tt.id IN (SELECT tgat.template_tache_id FROM template_groupe_association_tache tgat WHERE tgat.template_groupe_id = :templateGroupeId)",
           nativeQuery = true)
    long countByTemplateGroupeId(@Param("templateGroupeId") UUID templateGroupeId);
    
    java.util.Optional<TemplateTacheEntity> findByCode(String code);
}