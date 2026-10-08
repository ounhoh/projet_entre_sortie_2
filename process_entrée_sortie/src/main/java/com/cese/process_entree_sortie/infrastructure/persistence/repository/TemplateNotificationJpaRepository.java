 
package com.cese.process_entree_sortie.infrastructure.persistence.repository;

import com.cese.process_entree_sortie.infrastructure.persistence.entity.TemplateNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TemplateNotificationJpaRepository extends JpaRepository<TemplateNotificationEntity, UUID> {
    Optional<TemplateNotificationEntity> findByCodeNotification(String code);
    
    @Query("SELECT tn FROM TemplateNotificationEntity tn WHERE CAST(tn.niveau AS string) = :niveau")
    List<TemplateNotificationEntity> findByNiveau(@Param("niveau") String niveau);
    
    @Query("SELECT tn FROM TemplateNotificationEntity tn WHERE CAST(tn.typeEnvoyeur AS string) = :type")
    List<TemplateNotificationEntity> findByTypeEnvoyeur(@Param("type") String type);
}