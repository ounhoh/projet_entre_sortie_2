package com.cese.process_entree_sortie.infrastructure.persistence.repository;

import com.cese.process_entree_sortie.domain.utils.ValueObject.TypeProcessus;
import com.cese.process_entree_sortie.infrastructure.persistence.entity.InstanceProcessusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface InstanceProcessusJpaRepository extends JpaRepository<InstanceProcessusEntity, UUID> {
    List<InstanceProcessusEntity> findByAgentId(UUID agentId);
    
    @Query("SELECT ip FROM InstanceProcessusEntity ip WHERE ip.statutId = :statutId")
    List<InstanceProcessusEntity> findByStatutId(@Param("statutId") UUID statutId);
    
    @Query("SELECT ip FROM InstanceProcessusEntity ip WHERE ip.agentId = :agentId AND ip.statutId = :statutId")
    boolean existsByAgentIdAndStatutId(@Param("agentId") UUID agentId, @Param("statutId") UUID statutId);
    
    @Query("SELECT ip FROM InstanceProcessusEntity ip WHERE ip.statutId IN (SELECT sp.id FROM StatutProcessusEntity sp WHERE sp.codeStatut <> 'archive')")
    List<InstanceProcessusEntity> findActifs();
    
    @Query("SELECT ip FROM InstanceProcessusEntity ip WHERE ip.dateEcheance < :today AND ip.statutId NOT IN (SELECT sp.id FROM StatutProcessusEntity sp WHERE sp.codeStatut = 'archive')")
    List<InstanceProcessusEntity> findEnRetards(@Param("today") LocalDate today);
    
    List<InstanceProcessusEntity> findByDirectionConcerneeId(UUID directionId);
    
    @Query("SELECT COUNT(ip) FROM InstanceProcessusEntity ip WHERE ip.statutId = :statutId")
    long countByStatutId(@Param("statutId") UUID statutId);
    
    @Query("SELECT COUNT(ip) FROM InstanceProcessusEntity ip WHERE ip.typeProcessus = :type")
    long countByType(@Param("type") TypeProcessus type);
}