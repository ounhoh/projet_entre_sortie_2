package com.cese.process_entree_sortie.infrastructure.persistence.repository;

import com.cese.process_entree_sortie.infrastructure.persistence.entity.TemplateGroupeTacheAssociationEntity;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.TemplateGroupeTacheAssociationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TemplateGroupeTacheAssociationJpaRepository extends JpaRepository<TemplateGroupeTacheAssociationEntity, TemplateGroupeTacheAssociationId> {
    
    List<TemplateGroupeTacheAssociationEntity> findByTemplateGroupeId(UUID templateGroupeId);
    
    List<TemplateGroupeTacheAssociationEntity> findByTemplateTacheId(UUID templateTacheId);
    
    @Modifying
    @Query("DELETE FROM TemplateGroupeTacheAssociationEntity t WHERE t.templateGroupeId = :groupeId")
    void deleteByTemplateGroupeId(@Param("groupeId") UUID groupeId);
    
    @Modifying
    @Query("DELETE FROM TemplateGroupeTacheAssociationEntity t WHERE t.templateTacheId = :tacheId")
    void deleteByTemplateTacheId(@Param("tacheId") UUID tacheId);
}
