package com.cese.process_entree_sortie.infrastructure.persistence.repository;

import com.cese.process_entree_sortie.domain.utils.ValueObject.TypeProcessus;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.TemplateProcessusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TemplateProcessusJpaRepository extends JpaRepository<TemplateProcessusEntity, UUID> {
    @Query("SELECT tp FROM TemplateProcessusEntity tp WHERE tp.actif = true")
    List<TemplateProcessusEntity> findActifs();
    
    @Query("SELECT tp FROM TemplateProcessusEntity tp WHERE tp.type = :type")
    List<TemplateProcessusEntity> findByType(@Param("type") TypeProcessus type);
    
    java.util.Optional<TemplateProcessusEntity> findByCodeProcessus(String codeProcessus);
}