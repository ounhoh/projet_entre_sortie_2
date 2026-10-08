package com.cese.process_entree_sortie.infrastructure.persistence.repository;

import com.cese.process_entree_sortie.infrastructure.persistence.entity.InstanceNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface InstanceNotificationJpaRepository extends JpaRepository<InstanceNotificationEntity, UUID> {
    @Query("SELECT DISTINCT in FROM InstanceNotificationEntity in " +
           "JOIN in.recus r " +
           "JOIN AgentDirectionEntity ad ON r.agentDirectionId = ad.agentDirectionID " +
           "WHERE ad.agentPersonnelId = :agentId")
    List<InstanceNotificationEntity> findByAgentId(@Param("agentId") UUID agentId);
    
    @Query("SELECT DISTINCT in FROM InstanceNotificationEntity in " +
           "JOIN in.recus r " +
           "JOIN AgentDirectionEntity ad ON r.agentDirectionId = ad.agentDirectionID " +
           "WHERE ad.directionId = :directionId")
    List<InstanceNotificationEntity> findByDirectionId(@Param("directionId") UUID directionId);
    
    @Query("SELECT COUNT(DISTINCT in) FROM InstanceNotificationEntity in " +
           "JOIN in.recus r " +
           "JOIN AgentDirectionEntity ad ON r.agentDirectionId = ad.agentDirectionID " +
           "WHERE ad.agentPersonnelId = :agentId AND r.statutLecture = false")
    int countUnreadNotificationByAgentId(@Param("agentId") UUID agentId);

    List<InstanceNotificationEntity> findByGroupeTacheIdIn(List<UUID> groupeTacheIds);

    List<InstanceNotificationEntity> findByTacheIdIn(List<UUID> tacheIds);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM InstanceNotificationEntity in WHERE in.dateEnvoie < :before")
    void deleteOldNotifications(@Param("before") LocalDate before);
}