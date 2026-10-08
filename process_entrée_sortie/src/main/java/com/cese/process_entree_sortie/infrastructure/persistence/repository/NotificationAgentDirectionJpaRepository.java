package com.cese.process_entree_sortie.infrastructure.persistence.repository;

import com.cese.process_entree_sortie.infrastructure.persistence.entity.NotificationAgentDirectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import com.cese.process_entree_sortie.infrastructure.persistence.entity.NotificationAgentDirectionId;

@Repository
public interface NotificationAgentDirectionJpaRepository extends JpaRepository<NotificationAgentDirectionEntity, NotificationAgentDirectionId> {
    List<NotificationAgentDirectionEntity> findByNotificationId(UUID notificationId);
    
    List<NotificationAgentDirectionEntity> findByAgentDirectionId(UUID agentDirectionId);
    
    @Modifying
    @Transactional
    @Query("UPDATE NotificationAgentDirectionEntity nad SET nad.statutLecture = true WHERE nad.notificationId = :notificationId")
    void markAsRead(@Param("notificationId") UUID notificationId);
    
    @Modifying
    @Transactional
    @Query("UPDATE NotificationAgentDirectionEntity nad SET nad.statutLecture = true WHERE nad.agentDirectionId = :agentDirectionId")
    void markAllAsReadByAgentDirectionId(@Param("agentDirectionId") UUID agentDirectionId);
    
    @Modifying
    @Transactional
    @Query("UPDATE NotificationAgentDirectionEntity nad SET nad.statutLecture = true WHERE nad.agentDirectionId IN " +
           "(SELECT ad.agentDirectionID FROM AgentDirectionEntity ad WHERE ad.agentPersonnelId = :agentId)")
    void markAllAsReadByAgentId(@Param("agentId") UUID agentId);
}