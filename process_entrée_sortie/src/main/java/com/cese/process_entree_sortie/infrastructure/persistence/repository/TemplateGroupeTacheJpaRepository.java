package com.cese.process_entree_sortie.infrastructure.persistence.repository;

import com.cese.process_entree_sortie.infrastructure.persistence.entity.TemplateGroupeTacheEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TemplateGroupeTacheJpaRepository extends JpaRepository<TemplateGroupeTacheEntity, UUID> {
    List<TemplateGroupeTacheEntity> findByTemplateProcessusId(UUID processusId);
    List<TemplateGroupeTacheEntity> findByDirectionId(UUID directionId);
    
    long countByTemplateProcessusId(UUID processusId);
    
    java.util.Optional<TemplateGroupeTacheEntity> findByCodeTemplate(String codeTemplate);
}